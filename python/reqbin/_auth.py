import base64
import json
import os
import stat
from pathlib import Path

import msal

from ._config import ServerConfig

_CACHE_DIR = Path.home() / ".requestbinplusplus"
_CACHE_FILE = _CACHE_DIR / "token_cache.bin"


def _decode_jwt_payload(token: str) -> dict:
    """Decode JWT payload without signature verification."""
    try:
        payload_b64 = token.split(".")[1]
        # Pad base64 to a multiple of 4
        padding = 4 - len(payload_b64) % 4
        if padding != 4:
            payload_b64 += "=" * padding
        return json.loads(base64.urlsafe_b64decode(payload_b64))
    except Exception:
        return {}


class TokenManager:
    def __init__(self, server_config: ServerConfig) -> None:
        self._config = server_config
        self._cache = msal.SerializableTokenCache()

        if _CACHE_FILE.exists():
            self._cache.deserialize(_CACHE_FILE.read_text(encoding="utf-8"))

        self._app = msal.PublicClientApplication(
            client_id=server_config.client_id,
            authority=f"https://login.microsoftonline.com/{server_config.tenant_id}",
            token_cache=self._cache,
        )

        # Invalidate cache if it belongs to a different Entra app/tenant
        self._validate_cache_config()

    def _validate_cache_config(self) -> None:
        """
        Check cached tokens against current server config.
        If aud or tid mismatches, wipe the cache to force re-login.
        """
        accounts = self._app.get_accounts()
        if not accounts:
            return

        # Try to find any cached access token to inspect its payload
        for account in accounts:
            result = self._app.acquire_token_silent(
                scopes=[self._config.scope], account=account
            )
            if result and "access_token" in result:
                payload = _decode_jwt_payload(result["access_token"])
                aud = payload.get("aud", "")
                tid = payload.get("tid", "")
                if aud != self._config.client_id or tid != self._config.tenant_id:
                    print(
                        "[reqbin] Cached token does not match current server config. "
                        "Clearing cache and re-authenticating."
                    )
                    self._clear_cache()
                return

    def _save_cache(self) -> None:
        if self._cache.has_state_changed:
            _CACHE_DIR.mkdir(parents=True, exist_ok=True)
            _CACHE_FILE.write_text(self._cache.serialize(), encoding="utf-8")
            os.chmod(_CACHE_FILE, stat.S_IRUSR | stat.S_IWUSR)

    def _clear_cache(self) -> None:
        """Remove all accounts from cache and delete the cache file."""
        for account in self._app.get_accounts():
            self._app.remove_account(account)
        if _CACHE_FILE.exists():
            _CACHE_FILE.unlink()

    def _login(self) -> str:
        """
        Initiate Device Code Flow and block until the user authenticates.
        Returns the access token on success.
        """
        flow = self._app.initiate_device_flow(scopes=[self._config.scope])
        if "user_code" not in flow:
            raise RuntimeError(f"Failed to initiate device flow: {flow.get('error_description')}")

        # Print the user-facing message from MSAL (contains URL and code)
        print(flow["message"])

        result = self._app.acquire_token_by_device_flow(flow)
        if "access_token" not in result:
            raise RuntimeError(
                f"Authentication failed: {result.get('error_description', result.get('error'))}"
            )

        self._save_cache()
        return result["access_token"]

    def get_token(self) -> str:
        """
        Return a valid access token.
        Uses silent renewal from cache first; falls back to Device Code Flow.
        """
        accounts = self._app.get_accounts()
        if accounts:
            result = self._app.acquire_token_silent(
                scopes=[self._config.scope], account=accounts[0]
            )
            if result and "access_token" in result:
                self._save_cache()
                return result["access_token"]

        # No valid cached token — interactive login required
        return self._login()

    def logout(self) -> None:
        """Remove all cached credentials. Next API call will require re-login."""
        self._clear_cache()
        print("[reqbin] Logged out. Cached credentials removed.")

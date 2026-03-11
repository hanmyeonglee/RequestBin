from dataclasses import dataclass

import requests

from ._auth import TokenManager
from ._config import ServerConfig, bin_url_for, load_server_config


@dataclass(frozen=True)
class RequestInfo:
    method: str
    path: str
    query: dict[str, list[str]]
    headers: dict[str, str]
    body: bytes
    remote_host: str
    created_at: int


def _parse_request_info(data: dict) -> RequestInfo:
    return RequestInfo(
        method=data["method"],
        path=data["path"],
        query=data["query"],
        headers=data["headers"],
        body=data["body"],
        remote_host=data["remoteHost"],
        created_at=data["createdAt"],
    )


class RequestBin:
    """
    Client for the RequestBin++ API.

    Usage:
        client = RequestBin()           # loads server config, validates cached auth
        bin_id = client.create()        # creates a new bin (auto-login if needed)
        requests = client.read(10)      # reads up to 10 captured requests
        url = client.bin_url()          # returns the capture URL for this bin
        client.logout()                 # explicitly clear cached credentials
    """

    def __init__(self, api_url: str | None = None) -> None:
        self._api_url = (api_url or "https://api.requestbin.plus.or.kr").rstrip("/")
        self._server_config: ServerConfig = load_server_config(self._api_url)
        self._auth = TokenManager(self._server_config)
        self.bin_id: str | None = None

    def _auth_headers(self) -> dict[str, str]:
        return { "Authorization": f"Bearer {self._auth.get_token()}" }

    def create(self) -> str:
        """Create a new bin. Stores the bin url on this instance and returns it."""
        resp = requests.get(
            f"{self._api_url}/bin/create",
            headers=self._auth_headers(),
            timeout=10,
        )
        resp.raise_for_status()
        self.bin_id = resp.json()["binId"]
        return self.bin_id

    def read(self, num: int) -> list[RequestInfo]:
        """
        Read up to `num` captured requests from the current bin.
        Requires create() to have been called first.
        """
        if self.bin_id is None:
            raise RuntimeError("No bin created yet. Call create() first.")

        resp = requests.get(
            f"{self._api_url}/bin/read/{self.bin_id}/{num}",
            headers=self._auth_headers(),
            timeout=10,
        )
        resp.raise_for_status()
        return [_parse_request_info(item) for item in resp.json()]

    def bin_url(self) -> str:
        """
        Return the capture URL for the current bin.
        Requires create() to have been called first.
        """
        if self.bin_id is None:
            raise RuntimeError("No bin created yet. Call create() first.")
        return bin_url_for(self._api_url, self.bin_id)

    def logout(self) -> None:
        """Explicitly clear all cached credentials."""
        self._auth.logout()

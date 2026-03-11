from dataclasses import dataclass
from urllib.parse import urlparse

import requests


@dataclass(frozen=True)
class ServerConfig:
    tenant_id: str
    client_id: str
    scope: str


def load_server_config(api_url: str) -> ServerConfig:
    """Fetch Entra auth config from the server's public /config endpoint."""
    resp = requests.get(f"{api_url}/config", timeout=10)
    resp.raise_for_status()
    data = resp.json()
    return ServerConfig(
        tenant_id=data["tenantId"],
        client_id=data["clientId"],
        scope=data['clientId'],
    )


def bin_url_for(api_url: str, bin_id: str) -> str:
    """Derive bin capture URL from API URL by prepending bin_id as a subdomain."""
    parsed = urlparse(api_url)
    return f"{parsed.scheme}://{bin_id}.{parsed.netloc}"

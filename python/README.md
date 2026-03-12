# reqbin

Python client library for [RequestBin++](../README.md).

## Installation

```bash
pip install .
# or editable install for development
pip install -e .
# you can download from github
pip install git+https://github.com/hanmyeonglee/RequestBin.git#subdirectory=python
```

```bash
# or you can use uv
uv add git+https://github.com/hanmyeonglee/RequestBin.git#subdirectory=python
```

## Requirements

- Python 3.10+
- `msal >= 1.30`
- `requests >= 2.30`

## Usage

```python
from reqbin import RequestBin

client = RequestBin()

# Create a new bin (triggers login if not authenticated)
bin_id = client.create()
print(f"Bin ID  : {bin_id}")
print(f"Bin URL : {client.bin_url()}")

# Read up to 10 captured requests
requests = client.read(10)
for req in requests:
    print(req.method, req.path)
    print(req.headers)
    print(req.body)

# Explicitly log out and clear cached credentials
# If you want your session remained after code execution,
# don't use logout
client.logout()
```

## API Reference

### `RequestBin(api_url=None)`

Creates a client instance. Fetches server config immediately.

|Parameter|Type|Description|
|---|---|---|
|`api_url`|`str \| None`|Override API base URL. Default is `https://api.requestbin.plus.or.kr`|

### `create() -> str`

Creates a new request bin. Stores the bin ID on the instance and returns it.
Triggers Device Code Flow login if not authenticated.

### `read(num: int) -> list[RequestInfo]`

Reads up to `num` captured requests from the current bin.
Requires `create()` to have been called first.

### `bin_url() -> str`

Returns the capture URL for the current bin (e.g. `https://<binId>.requestbin.example.com`).
Requires `create()` to have been called first.

### `logout() -> None`

Clears all cached credentials from `~/.requestbin/token_cache.bin`.
Next API call will require interactive login.

### `RequestInfo`

|Field|Type|Description|
|---|---|---|
|`id`|`int`|Unique request ID|
|`method`|`str`|HTTP method|
|`path`|`str`|URL path|
|`query`|`dict[str, list[str]]`|Query string parameters|
|`headers`|`dict[str, str]`|Request headers (proxy headers excluded)|
|`body`|`bytes`|Request body (base64-decoded automatically)|
|`remote_host`|`str`|Remote client address|
|`created_at`|`int`|Unix timestamp (seconds)|

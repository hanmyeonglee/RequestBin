# RequestBin

Lightweight request capture service built with Scalatra and Scala 3.

## CORS policy

All requests are configured as allow-all CORS.

- `Access-Control-Allow-Origin: *`
- `Access-Control-Allow-Methods: *`
- `Access-Control-Allow-Headers: *`

## Run

- `sbt compile`
- `sbt run`

## Environment variables

- `REQUESTBIN_BASE_DOMAIN`: Base domain used for API and bin subdomain routing. Required.
- `PORT`: HTTP server port. Default `80`.
- `MAX_CONTENT_LENGTH`: Max request body size in bytes. Default `10485760` (10MB).
- `MAX_HEADER_SIZE`: Max header size in bytes. Default `10240` (10KB).
- `SECONDS_TO_LIVE`: Bin TTL in seconds. Default `900`.
- `CLEANUP_TIME_HOUR`: Daily cleanup trigger hour (0-23). Default `3`.
- `CLEANUP_INTERVAL_SECONDS`: Cleanup scheduler polling interval. Default `300`.
- `SCALATRA_ENV`: dev/production mode.
- `ENTRA_TENANT_ID`: microsoft ENTRA ID. Required.
- `ENTRA_CLIENT_ID`: microsoft app id(not client_secret). Required.
- `AUTH_NEEDED`

## API

This service uses host-based routing with `REQUESTBIN_BASE_DOMAIN`.

- API host (base domain): `https://<REQUESTBIN_BASE_DOMAIN>`
- Bin capture host (subdomain): `https://<binId>.<REQUESTBIN_BASE_DOMAIN>`

### Auth (for management APIs)

The following APIs require Microsoft Entra Bearer token:

- `GET /bin/create`
- `GET /bin/read/:binId/:numToRead`

Request header:

- `Authorization: Bearer <token>`

If token is missing/invalid, response is:

- Status: `401`
- Content-Type: `application/json; charset=UTF-8`
- Body: `{"error":"Unauthorized"}`

### 1. Create Bin

`GET /bin/create`

Create a new request bin.

#### Request

|field name|location|type|format|
|---|---|---|---|
|`Microsoft SSO JWT`|Header|string|`Authorization: Bearer <token>`|

#### Response

|field name|type|description|
|---|---|---|
|`binId`|string|Bin URL is `https://<binId>.<REQUESTBIN_BASE_DOMAIN>`|

Example:

```json
{"binId":"abcdefghijkl"}
```

### 2. Read Captured Requests

`GET /bin/read/:binId/:numToRead`

Read up to `numToRead` requests from a bin.

- `numToRead` must be integer and `>= 0`
- If `numToRead` is invalid, returns `400 Bad Request`
- If `binId` does not exist, returns `404 Not Found`

#### Request

|field name|location|type|format|
|---|---|---|---|
|`Microsoft SSO JWT`|Header|string|`Authorization: Bearer <token>`|
|`binId`|path|string|`/bin/read/abcdefghijkl/:numToRead`|
|`numToRead`|path|integer|`/bin/read/:binId/10`|

#### Response

Response is a JSON array of `RequestInfo`.

**RequestInfo**

|field name|type|description|
|---|---|---|
|`method`|string|HTTP Method|
|`path`|string|URL path|
|`query`|map<string, string[]>|URL query map. Each key maps to list of values.|
|`headers`|map<string, string>|HTTP header map (single value per header key). Reverse-proxy headers are excluded.|
|`body`|string|HTTP Body, base64 encoded string|
|`remoteHost`|string|addr of host who requests to bin|
|`createdAt`|int|unix seconds|

### 3. Capture Endpoint (Bin Subdomain)

For `https://<binId>.<REQUESTBIN_BASE_DOMAIN>`, any request path/method is treated as capture target.

- Route: `/*` on bin subdomain
- Method: all methods except `OPTIONS`
- Success: `200` with plain text remote IP
- Missing/expired bin: `404` with HTML body `<h1>Not Found</h1>`
- Malformed request payload parse failure: `400` with HTML body `<h1>Bad Request</h1>`
- Payload too large: `413` with HTML body `<h1>Payload Too Large</h1>`

### 4. Other Info

1. the size of request cannot exceed `MAX_CONTENT_LENGTH`
2. requestbin is valid until `SECONDS_TO_LIVE` seconds elapsed after last request

### Error Responses

Common non-auth error responses:

- `400`: `<h1>Bad Request</h1>`
- `404`: `<h1>Not Found</h1>`
- `413`: `<h1>Payload Too Large</h1>`
- `500`: `<h1>Internal Server Error</h1>`

## requestbin

Lightweight request capture service built with Scalatra and Scala 3.

### CORS policy

All requests are configured as allow-all CORS.

- `Access-Control-Allow-Origin: *`
- `Access-Control-Allow-Methods: *`
- `Access-Control-Allow-Headers: *`

### Run

- `sbt compile`
- `sbt run`

### Environment variables

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

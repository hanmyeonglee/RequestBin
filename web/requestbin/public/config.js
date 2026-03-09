// Dev-time defaults. In Docker, overwritten by entrypoint.sh at container start.
window.__config__ = {
    API_URL: 'http://api.requestbin.localhost',
    BIN_BASE_DOMAIN: 'api.requestbin.localhost',
    POLL_INTERVAL_MS: 10000,
    NUM_REQUESTS: 25,
    ENTRA_TENANT_ID: '',
    ENTRA_CLIENT_ID: '',
    ENTRA_SCOPE: '',
};

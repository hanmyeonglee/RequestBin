#!/bin/sh
# Inject runtime env into config.js before serving
cat > /app/dist/config.js <<EOF
window.__config__ = {
    API_URL: '${REQUESTBIN_API_URL:-http://localhost}',
    BIN_BASE_DOMAIN: '${REQUESTBIN_BIN_BASE_DOMAIN:-localhost}',
    POLL_INTERVAL_MS: ${REQUESTBIN_POLL_INTERVAL_MS:-15000},
    NUM_REQUESTS: ${REQUESTBIN_NUM_REQUESTS:-20},
    ENTRA_TENANT_ID: '${REQUESTBIN_ENTRA_TENANT_ID:-}',
    ENTRA_CLIENT_ID: '${REQUESTBIN_ENTRA_CLIENT_ID:-}',
    ENTRA_SCOPE: '${REQUESTBIN_ENTRA_SCOPE:-}',
};
EOF

exec npx serve -l 80

#!/bin/sh
# Inject runtime env into config.js before serving
cat > /app/dist/config.js <<EOF
window.__config__ = {
    API_URL: '${REQUESTBIN_API_URL:-http://localhost}',
    BIN_BASE_DOMAIN: '${REQUESTBIN_BIN_BASE_DOMAIN:-localhost}',
    POLL_INTERVAL_MS: ${REQUESTBIN_POLL_INTERVAL_MS:-15000},
    NUM_REQUESTS: ${REQUESTBIN_NUM_REQUESTS:-20},
};
EOF

# Get API_URL for CSP connect-src
API_URL="${REQUESTBIN_API_URL:-http://localhost}"

sed -i "s|PLACEHOLDER_API_URL|${API_URL}|g" /app/dist/serve.json

exec npx serve -l 80

export interface RequestInfo {
    method: string;
    path: string;
    query: Record<string, string[]>;
    headers: Record<string, string>;
    body: string; // base64 encoded
    remoteHost: string;
    createdAt: number; // unix seconds
}

declare global {
    interface Window {
        __config__: {
            API_URL: string;
            BIN_BASE_DOMAIN: string;
            POLL_INTERVAL_MS: number;
            NUM_REQUESTS: number;
            ENTRA_TENANT_ID: string;
            ENTRA_CLIENT_ID: string;
            ENTRA_SCOPE: string;
        };
    }
}

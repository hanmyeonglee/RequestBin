export interface RequestInfo {
    id: number;
    method: string;
    path: string;
    query: Record<string, string[]>;
    headers: Record<string, string>;
    body: string; // base64 encoded
    remoteHost: string;
    createdAt: number; // unix seconds
}

export interface EntraConfig {
    tenantId: string;
    clientId: string;
    scope: string;
}

declare global {
    interface Window {
        __config__: {
            API_URL: string;
            BIN_BASE_DOMAIN: string;
            POLL_INTERVAL_MS: number;
            NUM_REQUESTS: number;
        };
    }
}

import type {} from './types'; // pulls in Window.__config__ declaration

const cfg = window.__config__ ?? {};

export const API_URL: string = cfg.API_URL ?? '';
export const BIN_BASE_DOMAIN: string = cfg.BIN_BASE_DOMAIN ?? '';
export const POLL_INTERVAL_MS: number = cfg.POLL_INTERVAL_MS ?? 15000;
export const NUM_REQUESTS: number = cfg.NUM_REQUESTS ?? 20;
export const ENTRA_TENANT_ID: string = cfg.ENTRA_TENANT_ID ?? '';
export const ENTRA_CLIENT_ID: string = cfg.ENTRA_CLIENT_ID ?? '';
export const ENTRA_SCOPE: string = cfg.ENTRA_SCOPE ?? '';

export function binUrl(binId: string): string {
    return `${window.location.protocol}//${binId}.${BIN_BASE_DOMAIN}`;
}

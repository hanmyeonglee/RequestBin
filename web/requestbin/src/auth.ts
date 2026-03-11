import {
    PublicClientApplication,
    type Configuration,
    type AccountInfo,
    InteractionRequiredAuthError,
} from '@azure/msal-browser';
import { API_URL } from './config';
import type { EntraConfig } from './types';

let msalInstance: PublicClientApplication | null = null;
let entraScope = '';

// Must be called once on app startup.
// Fetches Entra config from /config, initializes MSAL, and handles redirect response.
export async function initAuth(): Promise<void> {
    const res = await fetch(`${API_URL}/config`);
    if (!res.ok) throw new Error(`Failed to load auth config: ${res.status}`);
    const { tenantId, clientId, scope } = await res.json() as EntraConfig;

    entraScope = scope;

    const msalConfig: Configuration = {
        auth: {
            clientId,
            authority: `https://login.microsoftonline.com/${tenantId}`,
            redirectUri: window.location.origin,
        },
        cache: {
            cacheLocation: 'localStorage',
        },
    };

    msalInstance = new PublicClientApplication(msalConfig);
    await msalInstance.initialize();
    await msalInstance.handleRedirectPromise();

    // Set active account from existing cache if available
    const accounts = msalInstance.getAllAccounts();
    if (accounts.length > 0) {
        msalInstance.setActiveAccount(accounts[0]);
    }
}

export function isAuthenticated(): boolean {
    return (msalInstance?.getAllAccounts().length ?? 0) > 0;
}

export function getAccount(): AccountInfo | null {
    return msalInstance?.getActiveAccount() ?? msalInstance?.getAllAccounts()[0] ?? null;
}

// Redirects to Microsoft login page. Returns after page reload.
export async function login(): Promise<void> {
    await msalInstance!.loginRedirect({ scopes: [entraScope] });
}

export async function logout(): Promise<void> {
    const account = getAccount();
    await msalInstance!.logoutRedirect({ account: account ?? undefined });
}

// Returns a valid access token, refreshing silently if needed.
// Falls back to redirect flow if silent renewal fails (e.g. consent required).
export async function getToken(): Promise<string> {
    const account = getAccount();
    if (!account) throw new Error('Not authenticated');

    try {
        const result = await msalInstance!.acquireTokenSilent({
            scopes: [entraScope],
            account,
        });
        return result.accessToken;
    } catch (e) {
        if (e instanceof InteractionRequiredAuthError) {
            // Silent renewal failed — redirect to interactive login
            await msalInstance!.acquireTokenRedirect({
                scopes: [entraScope],
                account,
            });
            // acquireTokenRedirect navigates away; this line is never reached
            throw new Error('Redirecting for token renewal');
        }
        throw e;
    }
}

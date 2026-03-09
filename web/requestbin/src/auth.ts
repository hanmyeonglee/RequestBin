import {
    PublicClientApplication,
    type Configuration,
    type AccountInfo,
    InteractionRequiredAuthError,
} from '@azure/msal-browser';
import { ENTRA_CLIENT_ID, ENTRA_SCOPE, ENTRA_TENANT_ID } from './config';

const msalConfig: Configuration = {
    auth: {
        clientId: ENTRA_CLIENT_ID,
        authority: `https://login.microsoftonline.com/${ENTRA_TENANT_ID}`,
        redirectUri: window.location.origin,
    },
    cache: {
        cacheLocation: 'localStorage',
    },
};

const msalInstance = new PublicClientApplication(msalConfig);

// Must be called once on app startup.
// Handles the redirect response if returning from Microsoft login.
export async function initAuth(): Promise<void> {
    await msalInstance.initialize();
    await msalInstance.handleRedirectPromise();

    // Set active account from existing cache if available
    const accounts = msalInstance.getAllAccounts();
    if (accounts.length > 0) {
        msalInstance.setActiveAccount(accounts[0]);
    }
}

export function isAuthenticated(): boolean {
    return msalInstance.getAllAccounts().length > 0;
}

export function getAccount(): AccountInfo | null {
    return msalInstance.getActiveAccount() ?? msalInstance.getAllAccounts()[0] ?? null;
}

// Redirects to Microsoft login page. Returns after page reload.
export async function login(): Promise<void> {
    await msalInstance.loginRedirect({ scopes: [ENTRA_SCOPE] });
}

export async function logout(): Promise<void> {
    const account = getAccount();
    await msalInstance.logoutRedirect({ account: account ?? undefined });
}

// Returns a valid access token, refreshing silently if needed.
// Falls back to redirect flow if silent renewal fails (e.g. consent required).
export async function getToken(): Promise<string> {
    const account = getAccount();
    if (!account) throw new Error('Not authenticated');

    try {
        const result = await msalInstance.acquireTokenSilent({
            scopes: [ENTRA_SCOPE],
            account,
        });
        return result.accessToken;
    } catch (e) {
        if (e instanceof InteractionRequiredAuthError) {
            // Silent renewal failed — redirect to interactive login
            await msalInstance.acquireTokenRedirect({
                scopes: [ENTRA_SCOPE],
                account,
            });
            // acquireTokenRedirect navigates away; this line is never reached
            throw new Error('Redirecting for token renewal');
        }
        throw e;
    }
}

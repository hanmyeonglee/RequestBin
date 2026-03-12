import { API_URL, NUM_REQUESTS } from './config';
import { getToken } from './auth';
import type { RequestInfo } from './types';

async function authHeaders(): Promise<HeadersInit> {
    const token = await getToken();
    return { Authorization: `Bearer ${token}` };
}

export async function createBin(): Promise<string> {
    const res = await fetch(`${API_URL}/bin/create`, {
        headers: await authHeaders(),
    });
    if (!res.ok) throw new Error(`Create bin failed: ${res.status}`);
    const { binId } = await res.json() as { binId: string };
    if (
        !binId || !/^[a-zA-Z0-9_-]+$/.test(binId)
    ) throw new Error(`Invalid binId: ${binId}`);
    return binId;
}

export async function readRequests(binId: string): Promise<RequestInfo[]> {
    const res = await fetch(`${API_URL}/bin/read/${binId}/${NUM_REQUESTS}`, {
        headers: await authHeaders(),
    });
    if (res.status === 404) return [];
    if (!res.ok) throw new Error(`Read requests failed: ${res.status}`);
    return res.json() as Promise<RequestInfo[]>;
}

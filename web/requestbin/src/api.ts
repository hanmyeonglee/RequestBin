import { API_URL, NUM_REQUESTS } from './config';
import type { RequestInfo } from './types';

export async function createBin(): Promise<string> {
    const res = await fetch(`${API_URL}/bin/create`);
    if (!res.ok) throw new Error(`Create bin failed: ${res.status}`);
    const data = await res.json() as { binId: string };
    return data.binId;
}

export async function readRequests(binId: string): Promise<RequestInfo[]> {
    const res = await fetch(`${API_URL}/bin/read/${binId}/${NUM_REQUESTS}`);
    if (res.status === 404) return [];
    if (!res.ok) throw new Error(`Read requests failed: ${res.status}`);
    return res.json() as Promise<RequestInfo[]>;
}

import { LitElement, html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import type { RequestInfo } from '../types';
import { tailwindSheet } from '../shared-styles';

function decodeBody(base64: string): string {
    try {
        const bytes = Uint8Array.from(atob(base64), c => c.charCodeAt(0));
        return new TextDecoder().decode(bytes);
    } catch {
        return '[binary data, cannot decode as UTF-8]';
    }
}

function formatDate(unix: number): string {
    return new Date(unix * 1000).toLocaleString();
}

@customElement('request-detail')
export class RequestDetail extends LitElement {
    connectedCallback(): void {
        super.connectedCallback();
        this.shadowRoot!.adoptedStyleSheets = [
            ...this.shadowRoot!.adoptedStyleSheets,
            tailwindSheet,
        ];
    }
    
    @property({ attribute: false }) request: RequestInfo | null = null;

    render() {
        if (!this.request) return html``;

        const req = this.request;
        const queryEntries = Object.entries(req.query || {});
        const headerEntries = Object.entries(req.headers || {});

        return html`
            <div class="h-full overflow-y-auto p-6 space-y-6 text-sm">
                <!-- Meta summary -->
                <div class="space-y-1">
                    <div class="flex items-center gap-3 flex-wrap">
                        <span class="font-mono font-bold text-cyan-600 uppercase">${req.method}</span>
                        <span class="font-mono text-slate-800 break-all">${req.path}</span>
                    </div>
                    <div class="text-xs text-slate-400">
                        ${req.remoteHost} &middot; ${formatDate(req.createdAt)}
                    </div>
                </div>

                <!-- Query params -->
                <div>
                    <h3 class="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Query</h3>
                    ${queryEntries.length > 0 ? html`
                        <table class="w-full text-xs font-mono border-collapse">
                            <tbody>
                                ${queryEntries.map(([k, vs]) => html`
                                    <tr class="border-t border-slate-100">
                                        <td class="py-1.5 pr-4 text-cyan-700 align-top w-1/3 break-all">${k}</td>
                                        <td class="py-1.5 text-slate-700 break-all">${vs.join(', ')}</td>
                                    </tr>
                                `)}
                            </tbody>
                        </table>
                    ` : html`<span class="text-xs font-mono text-slate-400">(empty)</span>`}
                </div>

                <!-- Headers -->
                <div>
                    <h3 class="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Headers</h3>
                    ${headerEntries.length > 0 ? html`
                        <table class="w-full text-xs font-mono border-collapse">
                            <tbody>
                                ${headerEntries.map(([k, v]) => html`
                                    <tr class="border-t border-slate-100">
                                        <td class="py-1.5 pr-4 text-cyan-700 align-top w-1/3 break-all">${k}</td>
                                        <td class="py-1.5 text-slate-700 break-all">${v}</td>
                                    </tr>
                                `)}
                            </tbody>
                        </table>
                    ` : html`<span class="text-xs font-mono text-slate-400">(empty)</span>`}
                </div>

                <!-- Body -->
                <div>
                    <h3 class="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Body</h3>
                    <div class="space-y-3">
                        <div>
                            <div class="text-xs text-slate-400 mb-1">base64</div>
                            <pre class="bg-slate-50 border border-slate-200 rounded p-3 text-xs font-mono text-slate-700 overflow-x-auto whitespace-pre-wrap break-all">${req.body || '(empty)'}</pre>
                        </div>
                        <div>
                            <div class="text-xs text-slate-400 mb-1">UTF-8 decoded</div>
                            <pre class="bg-slate-50 border border-slate-200 rounded p-3 text-xs font-mono text-slate-700 overflow-x-auto whitespace-pre-wrap break-all">${req.body ? decodeBody(req.body) : '(empty)'}</pre>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }
}

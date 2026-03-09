import { LitElement, html } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import { readRequests } from '../api';
import { binUrl, POLL_INTERVAL_MS } from '../config';
import type { RequestInfo } from '../types';
import './request-item';
import './request-detail';
import { tailwindSheet } from '../shared-styles.js';

@customElement('bin-viewer')
export class BinViewer extends LitElement {
    @property() binId!: string;
    @state() private requests: RequestInfo[] = [];
    @state() private selected: RequestInfo | null = null;
    @state() private loading = false;
    @state() private error: string | null = null;
    @state() private pollProgress = 0;

    private pollTimer: number | undefined;
    private progressTimer: number | undefined;
    private lastFetchAt = 0;

    connectedCallback() {
        super.connectedCallback();
        void this.fetchRequests();
        this.startProgressTimer();
        document.addEventListener('visibilitychange', this.onVisibilityChange);
        this.shadowRoot!.adoptedStyleSheets = [
            ...this.shadowRoot!.adoptedStyleSheets,
            tailwindSheet,
        ];
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        this.stopAll();
        document.removeEventListener('visibilitychange', this.onVisibilityChange);
    }

    private onVisibilityChange = () => {
        if (document.hidden) {
            this.stopAll();
        } else {
            const elapsed = Date.now() - this.lastFetchAt;
            if (elapsed >= POLL_INTERVAL_MS) {
                // Interval already passed while hidden — fetch immediately
                void this.fetchRequests();
            } else {
                // Schedule for the remaining time so cadence is preserved
                this.schedulePoll(POLL_INTERVAL_MS - elapsed);
            }
            this.startProgressTimer();
        }
    };

    // Schedule next fetch after `delay` ms (default: full interval)
    private schedulePoll(delay = POLL_INTERVAL_MS) {
        if (this.pollTimer !== undefined) clearTimeout(this.pollTimer);
        this.pollTimer = window.setTimeout(() => { void this.fetchRequests(); }, delay);
    }

    private startProgressTimer() {
        if (this.progressTimer !== undefined) clearInterval(this.progressTimer);
        this.progressTimer = window.setInterval(() => {
            this.pollProgress = Math.min((Date.now() - this.lastFetchAt) / POLL_INTERVAL_MS, 1);
        }, 250);
    }

    private stopAll() {
        if (this.pollTimer !== undefined) {
            clearTimeout(this.pollTimer);
            this.pollTimer = undefined;
        }
        if (this.progressTimer !== undefined) {
            clearInterval(this.progressTimer);
            this.progressTimer = undefined;
        }
    }

    private async fetchRequests() {
        if (this.loading) return;
        this.loading = true;
        this.error = null;
        try {
            const fresh = await readRequests(this.binId);

            // Skip state update if nothing has changed (suppress re-render)
            const newestOf = (list: RequestInfo[]) =>
                list.length === 0 ? 0 : Math.max(...list.map(r => r.createdAt));
            const unchanged =
                fresh.length === this.requests.length &&
                newestOf(fresh) === newestOf(this.requests);
            if (unchanged) return;

            // Re-match selected item into the fresh array so === comparison keeps working
            const prev = this.selected;
            this.requests = fresh;
            this.selected = prev
                ? (fresh.find(r =>
                    r.createdAt === prev.createdAt &&
                    r.method === prev.method &&
                    r.path === prev.path
                  ) ?? null)
                : null;
        } catch (e) {
            this.error = e instanceof Error ? e.message : 'Unknown error';
        } finally {
            this.loading = false;
            this.lastFetchAt = Date.now();
            this.pollProgress = 0;
            this.schedulePoll(); // recursive: schedule next after this one completes
        }
    }

    private onRequestSelect = (e: CustomEvent<RequestInfo>) => {
        this.selected = e.detail;
    };

    private copyUrl() {
        if (navigator.clipboard) {
            void navigator.clipboard.writeText(binUrl(this.binId));
        }
    }

    render() {
        const url = binUrl(this.binId);

        return html`
            <div class="flex h-screen bg-white">
                <!-- Left panel: URL bar + controls + request list -->
                <div class="w-96 shrink-0 flex flex-col border-r border-slate-200">
                    <div class="p-4 border-b border-slate-200 space-y-2">
                        <!-- Bin URL -->
                        <div class="flex items-center gap-2">
                            <input
                                readonly
                                .value=${url}
                                class="flex-1 text-xs font-mono bg-slate-50 border border-slate-200 rounded px-3 py-2 text-slate-700 truncate outline-none"
                            />
                            <button
                                @click=${this.copyUrl}
                                class="px-3 py-2 text-xs bg-cyan-600 hover:bg-cyan-700 text-white rounded transition-colors cursor-pointer"
                            >Copy</button>
                        </div>
                        <!-- Action buttons -->
                        ${(() => {
                            const r = 6;
                            const circ = 2 * Math.PI * r;
                            const dashOffset = circ * (1 - this.pollProgress);
                            return html`
                                <div class="flex gap-2">
                                    <button
                                        @click=${() => { void this.fetchRequests(); }}
                                        class="flex-1 flex items-center justify-center gap-1.5 px-3 py-1.5 text-xs border border-slate-200 hover:bg-slate-50 text-slate-600 rounded transition-colors cursor-pointer"
                                    >
                                        <svg width="14" height="14" viewBox="0 0 16 16" class="shrink-0">
                                            <circle cx="8" cy="8" r="${r}" fill="none" stroke="#e2e8f0" stroke-width="2.5"/>
                                            <circle cx="8" cy="8" r="${r}" fill="none" stroke="#22d3ee" stroke-width="2.5"
                                                stroke-dasharray="${circ} ${circ}"
                                                stroke-dashoffset="${dashOffset}"
                                                transform="rotate(-90 8 8)"
                                                stroke-linecap="round"
                                            />
                                        </svg>
                                        ${this.loading ? 'Loading…' : 'Refresh'}
                                    </button>
                                    <button
                                        @click=${() => this.dispatchEvent(new CustomEvent('bin-recreate', { bubbles: true, composed: true }))}
                                        class="flex-1 px-3 py-1.5 text-xs border border-cyan-200 hover:bg-cyan-50 text-cyan-600 rounded transition-colors cursor-pointer"
                                    >New Bin</button>
                                </div>
                            `;
                        })()}
                        ${this.error ? html`<div class="text-xs text-red-500">${this.error}</div>` : ''}
                    </div>

                    <!-- Request list -->
                    <div class="flex-1 overflow-y-auto">
                        ${this.requests.length === 0
                            ? html`<div class="p-8 text-sm text-slate-400 text-center">No requests yet</div>`
                            : this.requests.map(r => html`
                                <request-item
                                    .request=${r}
                                    .selected=${this.selected === r}
                                    @request-select=${this.onRequestSelect}
                                ></request-item>
                            `)
                        }
                    </div>
                </div>

                <!-- Right panel: request detail -->
                <div class="flex-1 overflow-hidden">
                    <request-detail .request=${this.selected}></request-detail>
                </div>
            </div>
        `;
    }
}

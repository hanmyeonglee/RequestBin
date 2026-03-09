import { LitElement, html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import type { RequestInfo } from '../types';
import { tailwindSheet } from '../shared-styles.js';

const METHOD_COLORS: Record<string, string> = {
    GET:    'bg-cyan-100 text-cyan-700',
    POST:   'bg-teal-100 text-teal-700',
    PUT:    'bg-slate-200 text-slate-700',
    PATCH:  'bg-slate-200 text-slate-600',
    DELETE: 'bg-red-100 text-red-600',
};

function methodColor(method: string): string {
    return METHOD_COLORS[method.toUpperCase()] ?? 'bg-slate-100 text-slate-600';
}

function formatTime(unix: number): string {
    return new Date(unix * 1000).toLocaleTimeString();
}

@customElement('request-item')
export class RequestItem extends LitElement {
    connectedCallback(): void {
        super.connectedCallback();
        this.shadowRoot!.adoptedStyleSheets = [
            ...this.shadowRoot!.adoptedStyleSheets,
            tailwindSheet,
        ];
    }
    
    @property({ attribute: false }) request!: RequestInfo;
    @property({ type: Boolean }) selected = false;

    private handleClick() {
        this.dispatchEvent(new CustomEvent('request-select', {
            detail: this.request,
            bubbles: true,
            composed: true,
        }));
    }

    render() {
        const base = 'flex items-center gap-3 px-4 py-3 cursor-pointer border-b border-slate-100 hover:bg-cyan-50 transition-colors';
        const cls = this.selected
            ? `${base} bg-cyan-50 border-l-2 border-l-cyan-500`
            : base;

        return html`
            <div class=${cls} @click=${this.handleClick}>
                <span class="text-xs font-mono font-semibold px-2 py-0.5 rounded min-w-13 text-center ${methodColor(this.request.method)}">
                    ${this.request.method}
                </span>
                <span class="flex-1 text-sm font-mono text-slate-700 truncate">${this.request.path}</span>
                <span class="text-xs text-slate-400 shrink-0">${formatTime(this.request.createdAt)}</span>
            </div>
        `;
    }
}

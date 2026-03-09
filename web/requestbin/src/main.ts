import { LitElement, html } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { createBin } from './api';
import './components/bin-viewer';

@customElement('main-page')
export class MainPage extends LitElement {
    protected createRenderRoot(): HTMLElement | DocumentFragment {
        return this;
    }

    @state() private binId: string | null = null;
    @state() private creating = false;

    private async handleCreate() {
        this.creating = true;
        try {
            this.binId = await createBin();
        } catch (e) {
            console.error('Failed to create bin:', e);
        } finally {
            this.creating = false;
        }
    }

    render() {
        if (this.binId) {
            return html`
                <bin-viewer
                    .binId=${this.binId}
                    @bin-recreate=${() => { this.binId = null; }}
                ></bin-viewer>
            `;
        }

        return html`
            <div class="min-h-screen flex flex-col items-center justify-center bg-white">
                <div class="text-center space-y-4">
                    <h1 class="text-2xl font-semibold text-slate-800">PLUS RequestBin</h1>
                    <p class="text-sm text-slate-500">Capture the HTTP requests</p>
                    <button
                        @click=${this.handleCreate}
                        ?disabled=${this.creating}
                        class="px-6 py-3 bg-cyan-600 hover:bg-cyan-700 disabled:bg-cyan-300 text-white font-medium rounded-lg transition-colors cursor-pointer"
                    >${this.creating ? 'Creating…' : 'Create Bin'}</button>
                </div>
            </div>
        `;
    }
}

import { LitElement, html } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { createBin } from './api';
import { initAuth, isAuthenticated, login } from './auth';
import './components/bin-viewer';

type AuthState = 'loading' | 'unauthenticated' | 'authenticated';

@customElement('main-page')
export class MainPage extends LitElement {
    protected createRenderRoot(): HTMLElement | DocumentFragment {
        return this;
    }

    @state() private authState: AuthState = 'loading';
    @state() private binId: string | null = null;
    @state() private creating = false;

    override async connectedCallback() {
        super.connectedCallback();
        try {
            await initAuth();
        } catch (e) {
            console.error('Auth initialization failed:', e);
        } finally {
            this.authState = isAuthenticated() ? 'authenticated' : 'unauthenticated';
        }
    }

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
        if (this.authState === 'loading') {
            return html`
                <div class="min-h-screen flex items-center justify-center bg-white">
                    <div class="text-sm text-slate-400">Loading…</div>
                </div>
            `;
        }

        if (this.authState === 'unauthenticated') {
            return html`
                <div class="min-h-screen flex flex-col items-center justify-center bg-white">
                    <div class="text-center space-y-4">
                        <h1 class="text-2xl font-semibold text-slate-800">PLUS RequestBin</h1>
                        <p class="text-sm text-slate-500">Sign in to continue</p>
                        <button
                            @click=${() => { void login(); }}
                            class="px-6 py-3 bg-cyan-600 hover:bg-cyan-700 text-white font-medium rounded-lg transition-colors cursor-pointer"
                        >Login with Microsoft</button>
                    </div>
                </div>
            `;
        }

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

import type { AuthState } from '../types/app';

const AUTH_KEY = 'ai-tool-collection.auth';

export function loadAuthState(): AuthState | null {
  const raw = window.localStorage.getItem(AUTH_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as AuthState;
  } catch {
    window.localStorage.removeItem(AUTH_KEY);
    return null;
  }
}

export function saveAuthState(state: AuthState) {
  window.localStorage.setItem(AUTH_KEY, JSON.stringify(state));
}

export function clearAuthState() {
  window.localStorage.removeItem(AUTH_KEY);
}

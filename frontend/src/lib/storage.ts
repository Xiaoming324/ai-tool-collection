import type { AuthState, CachedPdfFile } from '../types/app';

const AUTH_KEY = 'ai-tool-collection.auth';
const PDF_CACHE_KEY = 'ai-tool-collection.pdf-cache';

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

function loadPdfCacheMap() {
  const raw = window.localStorage.getItem(PDF_CACHE_KEY);
  if (!raw) {
    return {} as Record<string, CachedPdfFile>;
  }

  try {
    return JSON.parse(raw) as Record<string, CachedPdfFile>;
  } catch {
    window.localStorage.removeItem(PDF_CACHE_KEY);
    return {} as Record<string, CachedPdfFile>;
  }
}

export function getCachedPdfFile(chatId: string) {
  return loadPdfCacheMap()[chatId] ?? null;
}

export function cachePdfFile(file: CachedPdfFile) {
  const next = loadPdfCacheMap();
  next[file.chatId] = file;
  window.localStorage.setItem(PDF_CACHE_KEY, JSON.stringify(next));
}

export function removeCachedPdfFile(chatId: string) {
  const next = loadPdfCacheMap();
  delete next[chatId];
  window.localStorage.setItem(PDF_CACHE_KEY, JSON.stringify(next));
}

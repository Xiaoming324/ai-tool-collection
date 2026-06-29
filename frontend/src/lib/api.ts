import type { ApiResult, AuthForm, MessageItem, ModuleType, SendMessagePayload, SessionSummary } from '../types/app';

const API_PREFIX = '/api';

type RequestOptions = RequestInit & {
  token?: string | null;
};

async function request<T>(path: string, options: RequestOptions = {}) {
  const headers = new Headers(options.headers);

  if (options.token) {
    headers.set('Authorization', `Bearer ${options.token}`);
  }

  const response = await fetch(`${API_PREFIX}${path}`, {
    ...options,
    headers,
  });

  const contentType = response.headers.get('content-type') ?? '';
  const body = contentType.includes('application/json')
    ? await response.json() as ApiResult<T>
    : null;

  if (!response.ok || !body?.success) {
    throw new Error(body?.message || `Request failed with status ${response.status}`);
  }

  return body.data;
}

export function register(data: AuthForm) {
  return request<void>('/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });
}

export function login(data: AuthForm) {
  return request<string>('/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });
}

export function listSessions(type: ModuleType, token: string) {
  return request<SessionSummary[]>(`/ai/history/${type}`, {
    token,
  });
}

export function getMessages(type: ModuleType, chatId: string, token: string) {
  return request<MessageItem[]>(`/ai/history/${type}/${encodeURIComponent(chatId)}`, {
    token,
  });
}

export function createStreamRequest(
  path: string,
  payload: SendMessagePayload,
  token: string,
  method: 'GET' | 'POST',
) {
  if (method === 'GET') {
    const query = new URLSearchParams({
      prompt: payload.prompt,
      chatId: payload.chatId,
    });

    return fetch(`${API_PREFIX}${path}?${query.toString()}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  const formData = new FormData();
  formData.append('prompt', payload.prompt);
  formData.append('chatId', payload.chatId);
  payload.files?.forEach((file) => formData.append('files', file));

  return fetch(`${API_PREFIX}${path}`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: formData,
  });
}

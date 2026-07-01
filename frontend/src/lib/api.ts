import type {
  ApiResult,
  AuthForm,
  MessageItem,
  ModuleType,
  PdfSessionFile,
  PdfUploadResult,
  SessionSummary,
  TravelItinerary,
} from '../types/app';

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

function createFormData(data: Record<string, string>, files?: File[], fileField = 'files') {
  const formData = new FormData();
  Object.entries(data).forEach(([key, value]) => {
    formData.append(key, value);
  });
  files?.forEach((file) => {
    formData.append(fileField, file);
  });
  return formData;
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
  return request<SessionSummary[]>(`/ai/history/${type}`, { token });
}

export function getMessages(type: ModuleType, chatId: string, token: string) {
  return request<MessageItem[]>(`/ai/history/${type}/${encodeURIComponent(chatId)}`, { token });
}

export function deleteSession(type: ModuleType, chatId: string, token: string) {
  return request<void>(`/ai/history/${type}/${encodeURIComponent(chatId)}`, {
    method: 'DELETE',
    token,
  });
}

export function streamChatMessage(token: string, chatId: string, prompt: string, files: File[]) {
  return fetch(`${API_PREFIX}/ai/chat`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: createFormData({ prompt, chatId }, files),
  });
}

export function uploadPdf(token: string, chatId: string, file: File) {
  const formData = new FormData();
  formData.append('file', file);

  return request<PdfUploadResult>(`/ai/pdf/upload/${encodeURIComponent(chatId)}`, {
    method: 'POST',
    token,
    body: formData,
  });
}

export function getPdfPreviewUrl(token: string, fileId: number) {
  return request<string>(`/ai/pdf/file/${fileId}`, { token });
}

export function getPdfSessionFile(token: string, chatId: string) {
  return request<PdfSessionFile>(`/ai/pdf/session/${encodeURIComponent(chatId)}/file`, { token });
}

export function streamPdfMessage(token: string, chatId: string, prompt: string) {
  return fetch(`${API_PREFIX}/ai/pdf/chat`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: createFormData({ prompt, chatId }),
  });
}

export function streamTravelMessage(token: string, chatId: string, prompt: string) {
  return fetch(`${API_PREFIX}/ai/travel`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: createFormData({ prompt, chatId }),
  });
}

export function listTravelItineraries(token: string) {
  return request<TravelItinerary[]>('/ai/travel/itineraries', { token });
}

export function getTravelItinerary(token: string, itineraryId: number) {
  return request<TravelItinerary>(`/ai/travel/itineraries/${itineraryId}`, { token });
}

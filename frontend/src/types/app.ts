export type ModuleType = 'chat' | 'pdf' | 'travel';

export interface ApiResult<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface AuthForm {
  username: string;
  password: string;
}

export interface SessionSummary {
  id: number;
  chatId: string;
  title: string | null;
}

export interface MessageAttachment {
  fileId: number;
  fileKind: string;
  originalFilename: string;
  contentType?: string;
  sizeBytes?: number;
  url?: string;
}

export interface MessageItem {
  role: string;
  content: string;
  attachments?: MessageAttachment[];
}

export interface SendMessagePayload {
  chatId: string;
  prompt: string;
  files?: File[];
}

export interface AuthState {
  token: string;
  username: string;
}

export interface ModuleDefinition {
  type: ModuleType;
  label: string;
  kicker: string;
  heading: string;
  description: string;
  endpoint: string;
  requestMethod: 'GET' | 'POST';
  acceptsImages: boolean;
  emptyTitle: string;
  emptyDescription: string;
  composerPlaceholder: string;
  hint: string;
}

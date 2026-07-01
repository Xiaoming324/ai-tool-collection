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

export interface AuthState {
  token: string;
  username: string;
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
  attachments: MessageAttachment[];
}

export interface TravelItinerary {
  id: number;
  chatId: string;
  title: string;
  destination: string;
  startDate: string | null;
  endDate: string | null;
  itineraryContent: string;
  createdAt: string;
  updatedAt: string;
}

export interface PdfUploadResult {
  fileId: number;
  chatId: string;
  originalFilename: string;
}

export interface PdfSessionFile {
  fileId: number;
  chatId: string;
  originalFilename: string;
  url: string;
}

export interface CachedPdfFile {
  chatId: string;
  fileId: number;
  originalFilename: string;
}

export interface NavModule {
  type: ModuleType;
  label: string;
  path: string;
  summary: string;
  note: string;
}

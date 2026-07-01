import type { ModuleType, SessionSummary } from '../types/app';

export function createChatId(type: ModuleType) {
  const random = Math.random().toString(36).slice(2, 8);
  return `${type}-${Date.now().toString(36)}-${random}`;
}

export function getSessionLabel(session: SessionSummary) {
  const title = session.title?.trim();
  return title && title.length > 0 ? title : 'New conversation';
}

export function formatBytes(size?: number) {
  if (typeof size !== 'number' || Number.isNaN(size)) {
    return '';
  }
  if (size < 1024) {
    return `${size} B`;
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }
  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}

export function toMessageRoleLabel(role: string) {
  switch (role) {
    case 'assistant':
      return 'Lumi';
    case 'user':
      return 'You';
    case 'tool':
      return 'Tool';
    case 'system':
      return 'System';
    default:
      return role || 'Message';
  }
}

export function makeEmptyAssistantMessage() {
  return {
    role: 'assistant',
    content: '',
    attachments: [],
  };
}

export function createChatId(prefix: string) {
  const random = Math.random().toString(36).slice(2, 8);
  return `${prefix}-${Date.now().toString(36)}-${random}`;
}

export function getSessionTitle(chatId: string, title: string | null) {
  if (title && title.trim().length > 0) {
    return title;
  }
  return chatId;
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

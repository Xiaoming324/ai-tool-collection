export async function readTextStream(
  response: Response,
  onChunk: (chunk: string) => void,
) {
  if (!response.ok) {
    const message = await extractErrorMessage(response);
    throw new Error(message);
  }

  if (!response.body) {
    throw new Error('Streaming is not available in this browser.');
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();

  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }
    onChunk(decoder.decode(value, { stream: true }));
  }

  const tail = decoder.decode();
  if (tail) {
    onChunk(tail);
  }
}

async function extractErrorMessage(response: Response) {
  try {
    const contentType = response.headers.get('content-type') ?? '';
    if (contentType.includes('application/json')) {
      const data = await response.json() as { message?: string };
      return data.message || `Request failed with status ${response.status}`;
    }

    const text = await response.text();
    return text || `Request failed with status ${response.status}`;
  } catch {
    return `Request failed with status ${response.status}`;
  }
}

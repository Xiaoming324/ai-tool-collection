import type { ModuleDefinition } from '../types/app';

export const moduleDefinitions: Record<'chat' | 'pdf' | 'travel', ModuleDefinition> = {
  chat: {
    type: 'chat',
    label: 'Multimodal Chat',
    kicker: 'Module 2',
    heading: 'Persistent conversations with image reasoning',
    description:
      'Talk to Claude, upload images, and reopen the full conversation later with signed S3 image history.',
    endpoint: '/ai/chat',
    requestMethod: 'POST',
    acceptsImages: true,
    emptyTitle: 'Start a fresh multimodal thread',
    emptyDescription:
      'Send text only or attach multiple images. The backend will persist the session, messages, and file relationships.',
    composerPlaceholder: 'Ask Claude anything or attach images for visual analysis...',
    hint: 'Streams live from /ai/chat · stores sessions in chat_session · renders image history from signed S3 URLs',
  },
  pdf: {
    type: 'pdf',
    label: 'ChatPDF',
    kicker: 'Module 3',
    heading: 'Document workspace staged for the next backend slice',
    description:
      'The UI shell is ready, but PDF upload, preview, and RAG endpoints still need the backend implementation.',
    endpoint: '/ai/pdf/chat',
    requestMethod: 'GET',
    acceptsImages: false,
    emptyTitle: 'ChatPDF is not enabled yet',
    emptyDescription:
      'Once /ai/pdf/upload and /ai/pdf/chat are ready, this area can reuse the same session and message infrastructure.',
    composerPlaceholder: 'PDF chat will be enabled after the RAG backend lands...',
    hint: 'Reserved for Module 3 · PDF upload, preview, and retrieval are still pending on the server side',
  },
  travel: {
    type: 'travel',
    label: 'Travel Assistant',
    kicker: 'Module 4',
    heading: 'A function-calling workspace for trip planning',
    description:
      'The page already matches the planned /ai/travel contract, so you can wire it to the backend tool-calling flow as soon as it is ready.',
    endpoint: '/ai/travel',
    requestMethod: 'GET',
    acceptsImages: false,
    emptyTitle: 'Plan a trip with a persistent travel thread',
    emptyDescription:
      'Ask for destinations, itineraries, weather, and budget guidance. The frontend is ready to stream the answer as soon as the backend endpoint exists.',
    composerPlaceholder: 'Where do you want to go, and when?',
    hint: 'Targets /ai/travel · same session history API · graceful fallback if the backend route is not implemented yet',
  },
};

# AI Tool Collection Frontend

React + TypeScript + Vite frontend for `ai-tool-collection`.

## Covered now

- Auth: register, login, JWT persistence, route protection
- Module 2: multimodal chat
  - session history from `/ai/history/chat`
  - message history from `/ai/history/chat/{chatId}`
  - streaming requests to `/ai/chat`
  - image upload
  - signed S3 image URLs rendered in history
- Module 4: travel assistant workspace
  - session/history UI ready
  - streaming request contract wired to `/ai/travel`
  - graceful fallback if the backend route is not implemented yet
- Module 3: ChatPDF placeholder page, ready for the next backend slice

## Dev server

The Vite dev server proxies `/api` to `http://localhost:8080`.

## Run

```bash
npm install
npm run dev
```

## Build

```bash
npm run build
```

## Notes

- Backend must be running on `localhost:8080`
- This frontend expects the backend `Result<T>` format:

```json
{
  "success": true,
  "message": "ok",
  "data": {}
}
```

- Auth token is sent as:

```http
Authorization: Bearer <token>
```

# AI Tool Collection

A full-stack AI application built with Spring Boot 4 and React, demonstrating three practical AI capabilities: conversational chat with file attachments, PDF-based RAG (Retrieval-Augmented Generation), and an AI travel planning assistant powered by real-time tool calling.

---

## Features

### Chat
- Multi-turn conversation with Claude
- Session management (create, switch, delete conversations)
- File and image attachments stored on AWS S3
- Persistent message history in MySQL

### PDF RAG
- Upload PDF documents
- Automatic chunking and vector embedding via Aliyun DashScope
- Semantic search over uploaded documents using Redis as a vector store
- Claude answers questions grounded in your document content

### Travel Planning (Function Calling)
- AI travel assistant "Lumi" backed by 8 real-time tools:
  - **getWeather** — live forecast via Open-Meteo
  - **searchAttractions** — nearby points of interest via Wikipedia geosearch
  - **getDestinationGuide** — destination summaries via Wikipedia
  - **getCountryInfo** — currency, language, timezone via REST Countries
  - **getExchangeRate** — live exchange rates
  - **saveItinerary** — persist a generated itinerary to your account
  - **listMyTrips** — retrieve saved itineraries
  - **getTripDetail** — view a specific saved itinerary

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend framework | Spring Boot 4.1.0 |
| AI / LLM | Spring AI 2.0.0-M8 · Claude Sonnet (Anthropic) |
| Embeddings | Aliyun DashScope `text-embedding-v4` |
| Vector store | Redis Stack (RediSearch) |
| ORM | MyBatis-Plus 3.5 |
| Database | MySQL 8 |
| File storage | AWS S3 |
| Auth | JWT (jjwt 0.12) |
| Frontend | React 19 · TypeScript 6 · Vite 8 |
| Container | Docker Compose (Redis Stack) |
| Java version | 21 |

---

## Prerequisites

Make sure the following are installed and available before starting:

- **Java 21** — `java -version`
- **Maven 3.9+** — `mvn -version`
- **Node.js 20+** — `node -v`
- **Docker Desktop** (for Redis Stack)
- **MySQL 8** running locally on port `3306`
- An **Anthropic API key** (for Claude)
- An **Aliyun DashScope API key** (for embeddings)
- An **AWS S3** bucket with access key + secret key

---

## Project Structure

```
ai-tool-collection/
├── backend/                  # Spring Boot application
│   └── src/main/
│       ├── java/com/itheima/ai/
│       │   ├── config/       # Spring AI, S3, security config
│       │   ├── controller/   # REST endpoints
│       │   ├── entity/       # JPA/MyBatis entities and VOs
│       │   ├── filter/       # JWT auth filter
│       │   ├── mapper/       # MyBatis mappers
│       │   ├── service/      # Business logic
│       │   └── tool/         # Spring AI @Tool methods (travel)
│       └── resources/
│           ├── application.yaml          # Base config
│           └── application-local.yaml   # Secrets (gitignored)
├── frontend/                 # React + TypeScript SPA
│   └── src/
│       ├── components/       # Shared UI components
│       ├── contexts/         # Auth context
│       ├── lib/              # API client, stream utils
│       ├── pages/            # ChatPage, PdfPage, TravelPage, ...
│       └── types/            # TypeScript type definitions
├── docker-compose.yml        # Redis Stack
├── init.sql                  # Database schema
└── README.md
```

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/Xiaoming324/ai-tool-collection.git
cd ai-tool-collection
```

### 2. Initialize the database

Log in to MySQL and run the provided schema script:

```bash
mysql -u root -p < init.sql
```

This creates the `ai_tool_collection` database and all required tables.

### 3. Start Redis Stack

```bash
docker compose up -d
```

This starts:
- Redis on port `6379`
- RedisInsight (web UI) on port `8001` — open `http://localhost:8001` to inspect vector data

### 4. Configure backend secrets

Create `backend/src/main/resources/application-local.yaml` (this file is gitignored):

```yaml
spring:
  datasource:
    password: YOUR_MYSQL_PASSWORD
  ai:
    anthropic:
      api-key: YOUR_ANTHROPIC_API_KEY
    openai:
      api-key: YOUR_DASHSCOPE_API_KEY   # Aliyun DashScope key

cloud:
  aws:
    credentials:
      access-key: YOUR_AWS_ACCESS_KEY
      secret-key: YOUR_AWS_SECRET_KEY
    region:
      static: YOUR_AWS_REGION           # e.g. us-east-1
    s3:
      bucket: YOUR_S3_BUCKET_NAME

jwt:
  secret: YOUR_JWT_SECRET               # any long random string

travel:
  opentripmap:
    api-key: YOUR_OPENTRIPMAP_API_KEY   # optional, if using OpenTripMap
```

> **Never commit this file.** It is already excluded in `backend/.gitignore`.

### 5. Start the backend

```bash
cd backend
mvn spring-boot:run
```

The API server starts on **http://localhost:8080**.

### 6. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:5173**.

---

## Usage

1. Open **http://localhost:5173** in your browser.
2. Register a new account or log in.
3. Select a module from the hub:
   - **Chat** — general-purpose AI conversation with optional file uploads
   - **PDF** — upload a PDF, then ask questions about its content
   - **Travel** — describe a trip idea; Lumi will fetch live data and optionally save an itinerary

---

## API Overview

All endpoints require a `Bearer <token>` header except registration and login.

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Log in, receive JWT |
| `POST` | `/api/ai/chat` | Stream a chat response |
| `GET` | `/api/ai/history/chat` | List chat sessions |
| `GET` | `/api/ai/history/chat/{chatId}` | Get messages for a session |
| `DELETE` | `/api/ai/history/chat/{chatId}` | Delete a chat session |
| `POST` | `/api/ai/pdf/upload` | Upload a PDF for RAG |
| `POST` | `/api/ai/pdf/chat` | Stream a PDF Q&A response |
| `GET` | `/api/ai/history/pdf` | List PDF sessions |
| `POST` | `/api/ai/travel` | Stream a travel assistant response |
| `GET` | `/api/ai/travel/itineraries` | List saved itineraries |
| `GET` | `/api/ai/travel/itineraries/{id}` | Get a saved itinerary |

---

## Environment Notes

- **MySQL** must be running before starting the backend.
- **Redis** must be running before the backend starts (Spring AI initializes the vector index on startup).
- The backend and frontend communicate via `http://localhost:8080/api`. If you change the backend port, update `frontend/src/lib/api.ts`.

---

## License

MIT

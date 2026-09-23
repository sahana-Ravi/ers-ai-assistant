#  AI Assistant

An AI assistant for exploring ERS information through approved documentation and read-only database access. The application combines a Spring Boot and Spring AI backend with a React chat interface.

## Features

- Conversational chat through a React and Vite frontend.
- Retrieval-augmented generation (RAG) over PDF documents stored in `src/main/resources/documents/`.
- Qdrant vector search for relevant document context.
- Spring AI chat memory backed by H2.
- Read-only ERS database access through an MCP server.
- Redis semantic caching.
- Keycloak-ready OAuth2 resource-server configuration.
- MCP tool discovery through `GET /api/chat/tools`.

## Architecture

```text
React/Vite frontend :3000
        |
        | /api proxy
        v
Spring Boot API :8080
   |       |       |
   |       |       +--> MCP read-only database tools
   |       +----------> Qdrant document search :6334
   +------------------> H2 chat memory and Redis semantic cache
```

At startup, the backend reads PDF files from `src/main/resources/documents/`, splits them into chunks, and stores them in the `documents` Qdrant collection. Each chat request searches that collection and supplies the matching context to the model.

## Prerequisites

- Java 17 or later
- Maven 3.9+ or the included Maven wrapper
- Node.js 20+ and npm
- Docker Desktop with Docker Compose
- An OpenAI-compatible API key
- Access to the ERS database used by the MCP reader

## Quick start

### 1. Start local services

From the repository root:

```bash
docker compose up -d
```

This starts:

| Service | URL/Port | Purpose |
| --- | --- | --- |
| Keycloak | `http://localhost:7080` | Local identity provider |
| Redis | `localhost:6379` | Semantic cache |
| Qdrant REST API | `http://localhost:6333` | Vector store administration |
| Qdrant gRPC API | `localhost:6334` | Backend vector-store connection |

The default Keycloak development administrator is `admin` / `admin`. Change these credentials before using the stack outside local development.

### 2. Configure environment variables

The backend requires an OpenAI API key and the MCP database connection values:

PowerShell:

```powershell
$env:OPENAI_API_KEY = "your-api-key"
$env:SERVER_NAME = "your-sql-server"
$env:PASSWORD = "your-sql-password"
```

The MCP configuration is in `src/main/resources/mcp-servers.json`. Configure the server and credentials for your environment before starting the backend. The MCP database reader must remain read-only.

### 3. Run the backend

```powershell
.\mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080`.

### 4. Run the frontend

In a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000` in a browser. Vite forwards `/api` requests to the backend at `http://localhost:8080`.

## API

### Send a chat message

```http
POST /api/chat?message=What%20documents%20are%20available%3F
Accept: text/plain
```

The response is plain text generated from relevant RAG context, conversation memory, and permitted MCP tools.

### List available MCP tools

```http
GET /api/chat/tools
```

## Configuration

Main backend configuration is in `src/main/resources/application.yml`:

- `OPENAI_API_KEY`: model provider credential.
- `spring.ai.vectorstore.qdrant.*`: Qdrant host, port, and collection.
- `spring.datasource.*`: H2 chat-memory database.
- `spring.security.oauth2.resourceserver.jwt.issuer-uri`: Keycloak realm issuer.
- `spring.ai.mcp.client.*`: MCP client behavior and server configuration.

The default H2 file location is user-specific. Update `spring.datasource.url` if the application should store chat memory somewhere else.

## Development commands

From the repository root:

```powershell
.\mvnw.cmd test
```

From `frontend/`:

```powershell
npm run lint
npm run build
```

## Security notes

- The system prompt instructs the assistant to use ERS data in read-only mode and avoid exposing sensitive values or internal implementation details.
- Do not commit API keys, passwords, or production database connection values.
- The current `SecurityConfig` permits all HTTP requests for local development while still registering JWT resource-server support. Configure authenticated request rules before deploying to a shared or production environment.
- Replace the default Keycloak administrator credentials and use a dedicated realm and client configuration for deployment.

## Project structure

```text
src/main/java/       Spring Boot application, API, security, RAG, and configuration
src/main/resources/  Backend configuration, prompts, MCP configuration, and source documents
frontend/src/         React chat interface and API client
compose.yml           Local Keycloak, Redis, and Qdrant services
```

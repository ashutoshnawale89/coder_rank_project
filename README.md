# CodeRank — Online Code Execution Platform

CodeRank is an online code execution platform where users write, test, and run code
in multiple languages directly from the browser. Each submission is executed inside
an **isolated, resource-capped Docker container**, so untrusted code never reaches
the host.

> **Stack:** Java 17 · Spring Boot 3 · MySQL 8 · Docker · springdoc-openapi

---

## Table of Contents

1. [Features](#features)
2. [Architecture](#architecture)
3. [Tech Stack](#tech-stack)
4. [Project Structure](#project-structure)
5. [Prerequisites](#prerequisites)
6. [Quick Start (Docker Compose)](#quick-start-docker-compose)
7. [Run the Backend Locally](#run-the-backend-locally)
8. [Run the Frontend Locally](#run-the-frontend-locally)
9. [MySQL Setup](#mysql-setup)
10. [Environment Variables](#environment-variables)
11. [Swagger / OpenAPI](#swagger--openapi)
12. [API Reference](#api-reference)
13. [Request / Response Examples](#request--response-examples)
14. [Docker Sandbox Security Measures](#docker-sandbox-security-measures)
15. [Troubleshooting](#troubleshooting)

---

## Features

| # | Feature | Description |
|---|---------|-------------|
| 1 | **Multi-language execution** | Python, Java, JavaScript (Node.js). |
| 2 | **JWT Auth** | Register / login, BCrypt password hashing, stateless JWT. |
| 3 | **Sandboxed runs** | Each run uses a throwaway Docker container — no network, capped memory/CPU/PIDs, read-only FS, non-root user, dropped caps. |
| 4 | **Concurrency** | Bounded thread pool + queue; requests beyond capacity return 503. |
| 5 | **Rate limiting** | Per-user requests-per-minute on `/api/execute` (HTTP 429). |
| 6 | **Snippets** | Save / list / view / delete; users see only their own. |
| 7 | **Submission history** | Every execution stored with stdout, stderr, status, time. |
| 8 | **OpenAPI / Swagger UI** | Bearer-auth flow, endpoints grouped by Auth / Execution / Snippets / Submissions / Health. |

---

## Architecture

```
            ┌──────────────┐
            │  Web Client  │  (browser / Postman / Swagger UI)
            └──────┬───────┘
                   │ HTTPS - REST + JWT
                   ▼
            ┌──────────────────────────────┐
            │       Spring Boot API        │
            │ ─ JwtAuthenticationFilter    │
            │ ─ RateLimiterService         │
            │ ─ ExecutionService           │
            │      └─ Bounded ThreadPool   │
            │            └─ DockerExecutor │
            └───┬───────────────────────┬──┘
                │                       │
                ▼                       ▼
        ┌────────────┐         ┌───────────────────────┐
        │  MySQL 8   │         │   Docker Engine       │
        │ users      │         │   per-run sandbox     │
        │ snippets   │         │   python / java / node│
        │ submissions│         │   --network none ...  │
        └────────────┘         └───────────────────────┘
```

**Execution flow**

1. Client `POST /api/execute` with JWT, language, code, optional stdin.
2. JWT filter authenticates, rate limiter checks per-user quota.
3. Request is queued onto the bounded execution thread pool.
4. `DockerExecutor` writes code to a temp dir and runs `docker run --rm --network none --memory ... --cpus ... --pids-limit ... --read-only --cap-drop ALL --security-opt no-new-privileges --user 1000:1000 ...`.
5. stdout, stderr, exit code, and wall-clock time are captured. On timeout the container is force-killed.
6. The result is persisted as a `Submission` and returned.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.3, Spring Security, Spring Data JPA |
| Database | MySQL 8 |
| Auth | JWT (HS256 via jjwt 0.12) |
| API Docs | springdoc-openapi 2.6 |
| Sandbox | Docker (host engine via mounted socket) |
| Build | Maven |
| Orchestration | Docker Compose |

---

## Project Structure

```
coder_rank_project/
├── docker-compose.yml
├── README.md
└── SpringBootBackend/
    ├── Dockerfile
    ├── .dockerignore
    ├── .env.example
    ├── pom.xml
    └── src/main/
        ├── java/com/code/rank/
        │   ├── Application.java
        │   ├── config/        SecurityConfig, SwaggerConfig, ExecutorConfig
        │   ├── controller/    Auth, Execution, Snippet, Submission, Health
        │   ├── dto/
        │   │   ├── request/   RegisterRequest, LoginRequest, ExecuteRequest, SnippetRequest
        │   │   └── response/  ApiResponse, ErrorResponse, AuthResponse,
        │   │                  ExecuteResponse, SnippetResponse, SubmissionResponse
        │   ├── entity/        User, Snippet, Submission, Language, ExecutionStatus
        │   ├── exception/     GlobalExceptionHandler + typed exceptions
        │   ├── executor/      DockerExecutor, LanguageSpec, ExecutionResult, StreamGobbler
        │   ├── repository/    UserRepository, SnippetRepository, SubmissionRepository
        │   ├── security/      JwtTokenProvider, JwtAuthenticationFilter,
        │   │                  CustomUserDetails, UserDetailsServiceImpl,
        │   │                  RestAuthenticationEntryPoint
        │   └── service/       AuthService, ExecutionService, SnippetService,
        │                      SubmissionService, RateLimiterService
        └── resources/
            └── application.properties
```

---

## Prerequisites

- Docker 24+ and Docker Compose v2 (for the Quick Start)
- For local backend dev: JDK 17 and Maven 3.9+
- ~4 GB free RAM (MySQL + API + language images)
- Free ports: **8080** (API), **3306** (MySQL)

---

## Quick Start (Docker Compose)

```bash
# 1. Copy env template and edit secrets
cp SpringBootBackend/.env.example .env
#    Set DB_PASSWORD and JWT_SECRET to strong values

# 2. Build and start everything
docker compose up --build
```

| Service | URL | Purpose |
|---------|-----|---------|
| backend | http://localhost:8080 | Spring Boot REST API |
| mysql   | localhost:3306        | Database |
| Swagger | http://localhost:8080/swagger-ui/index.html | API docs |

Pre-pull the language images on the host (the backend reuses the host engine):

```bash
docker pull python:3.11-slim
docker pull eclipse-temurin:17-jdk-jammy
docker pull node:18-alpine
```

Stop and remove everything (including DB volume):

```bash
docker compose down -v
```

---

## Run the Backend Locally

```bash
cd SpringBootBackend
# Make sure MySQL is reachable (see "MySQL Setup")
mvn clean install
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Override settings via environment
variables (see `.env.example`) or by editing `application.properties`.

---

## Run the Frontend Locally

This repository contains the backend only. A separate React/Vue/etc. frontend
can call the API on `http://localhost:8080/api`. Typical commands once a
`frontend/` directory exists:

```bash
cd frontend
npm install
npm run dev
```

Point the client at `VITE_API_BASE_URL=http://localhost:8080`.

---

## MySQL Setup

Option A — use the Compose service (recommended). Compose starts a MySQL 8
container with the `coder_rank` database pre-created.

Option B — local MySQL:

```sql
CREATE DATABASE coder_rank CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'coderank'@'%' IDENTIFIED BY 'change_me';
GRANT ALL PRIVILEGES ON coder_rank.* TO 'coderank'@'%';
FLUSH PRIVILEGES;
```

Then set `DB_USERNAME` / `DB_PASSWORD` / `DB_URL` accordingly.
Schema is created automatically (`spring.jpa.hibernate.ddl-auto=update`).

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:mysql://localhost:3306/coder_rank?...` | JDBC URL |
| `DB_USERNAME` | `root` | DB user |
| `DB_PASSWORD` | *(required)* | DB password |
| `JWT_SECRET` | *(required, base64, ≥ 32 bytes)* | JWT signing key |
| `JWT_EXPIRATION_MS` | `86400000` | Token lifetime (ms) |
| `DOCKER_ENABLED` | `true` | Enable Docker sandbox |
| `DOCKER_TIMEOUT` | `10` | Per-run timeout (s) |
| `DOCKER_MEMORY` | `256m` | Container memory cap |
| `DOCKER_CPUS` | `0.5` | Container CPU cap |
| `DOCKER_PIDS_LIMIT` | `64` | Max processes in container |
| `DOCKER_WORKDIR` | `/tmp/coderank` | Host scratch dir for code |
| `EXECUTOR_CORE` | `4` | Core threads |
| `EXECUTOR_MAX` | `8` | Max threads |
| `EXECUTOR_QUEUE` | `20` | Queue capacity before 503 |
| `RATE_LIMIT_RPM` | `10` | /api/execute requests per user per minute |

**Never commit `.env`.** Use `.env.example` as the template.

---

## Swagger / OpenAPI

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

Groups: **Auth · Execution · Snippets · Submissions · Health**.

### How to authorize in Swagger

1. Call `POST /api/auth/register` to create a user.
2. Call `POST /api/auth/login` and copy the `token` from the response.
3. Click the **Authorize** button (top right in Swagger UI).
4. Enter: `Bearer <token>` (the prefix `Bearer ` is required).
5. Click **Authorize**, close the dialog. All protected endpoints will now send the token.

---

## API Reference

Base URL: `http://localhost:8080/api`. All endpoints except `/auth/**` and
`/health` require `Authorization: Bearer <token>`.

### Auth

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | public | Create a new user |
| POST | `/api/auth/login`    | public | Login, returns JWT |

### Execution

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/execute` | JWT | Run code; rate-limited per user |

### Snippets

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST   | `/api/snippets`        | JWT | Create snippet |
| GET    | `/api/snippets`        | JWT | List caller's snippets (pageable) |
| GET    | `/api/snippets/{id}`   | JWT | Get snippet (owner only) |
| DELETE | `/api/snippets/{id}`   | JWT | Delete snippet (owner only) |

### Submissions

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/submissions`      | JWT | List caller's submissions (pageable) |
| GET | `/api/submissions/{id}` | JWT | Get submission (owner only) |

### Health

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/health` | public | Liveness probe |

---

## Request / Response Examples

### Register

`POST /api/auth/register`
```json
{ "username": "alice", "email": "alice@example.com", "password": "secret123" }
```
Response:
```json
{
  "success": true,
  "message": "User registered",
  "data": {
    "token": "eyJhbGciOiJIUzI1...",
    "tokenType": "Bearer",
    "expiresInMs": 86400000,
    "userId": 1,
    "username": "alice"
  },
  "timestamp": "2026-05-23T10:15:30Z"
}
```

### Login

`POST /api/auth/login`
```json
{ "username": "alice", "password": "secret123" }
```

### Execute Code

`POST /api/execute` (header: `Authorization: Bearer <token>`)
```json
{
  "language": "PYTHON",
  "code": "print(sum(range(10)))",
  "stdin": ""
}
```
Response:
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "submissionId": 42,
    "language": "PYTHON",
    "status": "SUCCESS",
    "stdout": "45\n",
    "stderr": "",
    "exitCode": 0,
    "executionTimeMs": 312
  },
  "timestamp": "2026-05-23T10:16:00Z"
}
```

`status` values: `SUCCESS`, `COMPILE_ERROR`, `RUNTIME_ERROR`, `TIMEOUT`, `INTERNAL_ERROR`.

### Create Snippet

`POST /api/snippets`
```json
{ "title": "Hello World", "language": "JAVASCRIPT", "code": "console.log('hi')" }
```

### Error format

```json
{
  "success": false,
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required",
  "path": "/api/snippets",
  "timestamp": "2026-05-23T10:17:00Z"
}
```

Validation errors include `details` listing the failing fields.

| Code | Meaning |
|------|---------|
| 400 | Validation failed / unsupported language |
| 401 | Missing or invalid JWT |
| 403 | Accessing another user's resource |
| 404 | Resource not found |
| 429 | Rate limit exceeded |
| 503 | Execution queue full |
| 500 | Internal / Docker engine error |

---

## Docker Sandbox Security Measures

Untrusted code is hostile by default. Every sandbox container is launched with:

- `--network none` — no network at all
- `--memory` / `--memory-swap` — memory capped, swap disabled
- `--cpus` — CPU share capped
- `--pids-limit` — fork-bomb protection
- `--read-only` root FS + small `tmpfs` for scratch
- `--cap-drop ALL` — all Linux capabilities removed
- `--security-opt no-new-privileges` — privilege escalation blocked
- `--user 1000:1000` — non-root inside the container
- Code is mounted **read-only** (`/sandbox:ro`)
- `--rm` — container is destroyed after each run
- Hard wall-clock timeout; container is force-killed on overrun and the temp dir is removed

Platform-level protections:

- JWT authentication on every non-auth endpoint
- Owner-only authorization on snippets and submissions
- Per-user rate limiting (HTTP 429 on overflow)
- Bounded execution queue (HTTP 503 on overflow) — host can't be DoS'd by request floods
- Input validation on length, language, and required fields
- BCrypt password hashing
- Secrets via environment variables — never committed

### Docker socket warning

Compose mounts `/var/run/docker.sock` into the backend so it can launch
sandbox containers. **This effectively grants the backend root on the host.**
Only run this configuration in trusted environments. For production:

- run the backend on a dedicated host that you do not share with other services,
- or run sandboxes in a remote Docker-in-Docker service / Firecracker / gVisor,
- or move execution behind a separate hardened worker.

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `Failed to configure a DataSource` | DB_URL / DB_USERNAME / DB_PASSWORD missing or wrong; check `.env`. |
| `Cannot connect to the Docker daemon` | Ensure Docker is running and the socket is mounted into the backend container. |
| `JWT secret must be at least 256 bits` | `JWT_SECRET` is too short; provide a base64 value ≥ 32 bytes. |
| First execution is very slow | Language images pulled on first use — pre-pull with `docker pull`. |
| `429 Too Many Requests` | Per-user rate limit hit; wait a minute or raise `RATE_LIMIT_RPM`. |
| `503 Service Unavailable` on /execute | Execution queue is saturated; raise `EXECUTOR_QUEUE` or scale horizontally. |
| MySQL `Public Key Retrieval is not allowed` | Append `&allowPublicKeyRetrieval=true` to `DB_URL` (dev only). |
| Backend exits with DB connection refused | MySQL is still starting; Compose retries via `depends_on` healthcheck — wait and re-check. |

---

## License

Released under the MIT License.

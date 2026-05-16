# CodeRank — Online Code Execution Platform

CodeRank is a full-stack online code execution platform that lets users write, test,
and run code in multiple programming languages directly from the browser — no local
setup required. Each submission is executed inside an **isolated, resource-capped
Docker container**, so untrusted code can never reach the host or other users.

> **Stack:** Java 17 · Spring Boot 3 · React 18 · MySQL 8 · Docker

---

## Table of Contents

1. [Features](#features)
2. [Architecture](#architecture)
3. [Tech Stack](#tech-stack)
4. [Project Structure](#project-structure)
5. [Prerequisites](#prerequisites)
6. [Quick Start (Docker)](#quick-start-docker)
7. [Local Development](#local-development)
8. [Configuration](#configuration)
9. [API Documentation](#api-documentation)
10. [Security Measures](#security-measures)
11. [Design Decisions](#design-decisions)
12. [Troubleshooting](#troubleshooting)

---

## Features

| # | Feature | Description |
|---|---------|-------------|
| 1 | **Multi-language support** | Run code in **Python, Java and JavaScript (Node.js)**. New languages are added via a single config entry. |
| 2 | **Code Execution API** | RESTful endpoints to submit a snippet and receive `stdout`, `stderr`, exit code, and execution time. |
| 3 | **Sandboxed execution** | Every run happens in a throwaway Docker container with no network, a read-only filesystem, dropped capabilities, and a non-root user. |
| 4 | **Concurrency handling** | A bounded thread pool + queue lets the platform serve many simultaneous submissions without overloading the host. |
| 5 | **Timeout & error handling** | Hard wall-clock timeout per run; compile errors, runtime errors, and timeouts are reported back with clear messages. |
| 6 | **Resource management** | CPU, memory, process count (PIDs), and disk are capped per container. |
| 7 | **Authentication & authorization** | JWT-based auth. Users can only view and manage their own snippets and submissions. |
| 8 | **Rate limiting** | Per-user request throttling to prevent abuse of the execution service. |
| 9 | **Snippet history** | Code snippets and execution results are persisted in MySQL and browsable in the UI. |

---

## Architecture

```
                ┌──────────────┐
                │  React SPA   │   Browser UI: editor, language picker,
                │  (Nginx)     │   results pane, history
                └──────┬───────┘
                       │ HTTPS / REST + JWT
                       ▼
                ┌──────────────┐
                │ Spring Boot  │   AuthController · SnippetController
                │  REST API    │   ExecutionController
                │              │   ─ JWT filter · Rate limiter
                │              │   ─ Execution thread pool
                └───┬──────┬───┘
                    │      │
        ┌───────────┘      └────────────┐
        ▼                               ▼
 ┌─────────────┐               ┌────────────────────┐
 │   MySQL 8   │               │  Docker Engine     │
 │ users,      │               │  (per-run sandbox  │
 │ snippets,   │               │   containers)      │
 │ submissions │               │  python / java /   │
 └─────────────┘               │  node / gcc images │
                                └────────────────────┘
```

**Execution flow**

1. Client submits code + language to `POST /api/execute`.
2. The API authenticates the JWT and checks the rate limit.
3. The request is handed to a bounded executor service.
4. A worker writes the code to a temp directory and launches a hardened
   Docker container (`docker run --rm --network none --memory ... --cpus ...`).
5. The container compiles/runs the code; `stdout`/`stderr` are captured.
6. If the wall-clock timeout is exceeded, the container is force-killed.
7. The result is persisted to MySQL and returned to the client.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, React Router, Axios, Monaco Editor, Vite |
| Backend | Java 17, Spring Boot 3, Spring Security, Spring Data JPA |
| Database | MySQL 8 |
| Auth | JWT (HS256) |
| Sandbox | Docker (Docker-out-of-Docker via mounted socket) |
| Build | Maven (backend), npm/Vite (frontend) |
| Orchestration | Docker Compose |

---

## Project Structure

```
coderank/
├── backend/                 # Spring Boot application
│   ├── src/main/java/...
│   │   ├── controller/      # Auth, Snippet, Execution REST controllers
│   │   ├── service/         # ExecutionService, DockerRunner, AuthService
│   │   ├── model/           # User, Snippet, Submission entities
│   │   ├── repository/      # Spring Data JPA repositories
│   │   ├── security/        # JWT filter, rate limiter, config
│   │   └── config/          # Language definitions, executor pool
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                # React single-page app
│   ├── src/
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## Prerequisites

- **Docker** 24+ and **Docker Compose** v2
- ~4 GB free RAM (database + API + language images)
- Ports **3306**, **8080**, and **5173** (or **80**) free

For local (non-Docker) development you additionally need JDK 17, Maven 3.9+,
and Node.js 18+.

---

## Quick Start (Docker)

This is the recommended way to run CodeRank — everything is containerized.

```bash
# 1. Clone the repository
git clone https://github.com/<your-username>/coderank.git
cd coderank

# 2. Create the environment file
cp .env.example .env
#    Edit .env and set a strong JWT_SECRET and DB password.

# 3. Build and start all services
docker compose up --build
```

Docker Compose starts four things:

| Service    | URL                      | Purpose                         |
|------------|--------------------------|---------------------------------|
| `frontend` | http://localhost:5173    | React UI                        |
| `backend`  | http://localhost:8080    | Spring Boot REST API            |
| `mysql`    | localhost:3306           | Database                        |
| Language images | pulled on first run | `python:3.12-slim`, `openjdk:17`, `node:18-slim`, `gcc:13` |

Once the logs show `Started CodeRankApplication`, open
**http://localhost:5173**, register an account, and run your first snippet.

To stop and remove everything (including the database volume):

```bash
docker compose down -v
```

> **Note on the Docker socket:** the backend container mounts the host Docker
> socket (`/var/run/docker.sock`) so it can launch sandbox containers. This is
> required for code execution. See [Security Measures](#security-measures) for
> how the sandboxes themselves are locked down.

---

## Local Development

Run the database in Docker and the apps natively for faster iteration.

```bash
# Database only
docker compose up mysql

# Backend (terminal 2)
cd backend
mvn spring-boot:run

# Frontend (terminal 3)
cd frontend
npm install
npm run dev
```

Backend runs on `:8080`, frontend dev server on `:5173` with a proxy to the API.

---

## Configuration

All configuration is supplied through environment variables (`.env`).

| Variable | Description | Example |
|----------|-------------|---------|
| `MYSQL_DATABASE` | Database name | `coderank` |
| `MYSQL_USER` | Application DB user | `coderank` |
| `MYSQL_PASSWORD` | Application DB password | `change-me` |
| `MYSQL_ROOT_PASSWORD` | MySQL root password | `change-me-too` |
| `JWT_SECRET` | Secret for signing JWTs (≥ 32 chars) | `a-long-random-string` |
| `JWT_EXPIRATION_MS` | Token lifetime in ms | `86400000` |
| `EXECUTION_TIMEOUT_SECONDS` | Hard per-run timeout | `10` |
| `EXECUTION_MEMORY_LIMIT` | Memory cap per container | `256m` |
| `EXECUTION_CPU_LIMIT` | CPU cap per container | `0.5` |
| `EXECUTION_POOL_SIZE` | Concurrent executions allowed | `8` |
| `RATE_LIMIT_PER_MINUTE` | Max executions per user/min | `20` |

---

## API Documentation

Base URL: `http://localhost:8080/api`
All endpoints except `/auth/**` require an `Authorization: Bearer <token>` header.

Interactive Swagger UI is available at **http://localhost:8080/swagger-ui.html**.

### Authentication

#### `POST /api/auth/register`
Create a new account.

```json
{ "username": "alice", "email": "alice@example.com", "password": "secret123" }
```
**201 Created** → `{ "id": 1, "username": "alice" }`

#### `POST /api/auth/login`
Authenticate and receive a JWT.

```json
{ "username": "alice", "password": "secret123" }
```
**200 OK** → `{ "token": "eyJhbGciOi...", "expiresIn": 86400000 }`

### Code Execution

#### `POST /api/execute`
Submit code for execution.

```json
{
  "language": "python",
  "code": "print(sum(range(10)))",
  "stdin": ""
}
```
**200 OK**
```json
{
  "submissionId": 42,
  "status": "SUCCESS",
  "stdout": "45\n",
  "stderr": "",
  "exitCode": 0,
  "executionTimeMs": 312
}
```

Possible `status` values: `SUCCESS`, `COMPILE_ERROR`, `RUNTIME_ERROR`,
`TIMEOUT`, `MEMORY_EXCEEDED`.

#### `GET /api/execute/languages`
List supported languages.
**200 OK** → `["python", "java", "javascript", "cpp"]`

### Snippets

| Method & Path | Description |
|---------------|-------------|
| `POST /api/snippets` | Save a code snippet |
| `GET /api/snippets` | List the current user's snippets |
| `GET /api/snippets/{id}` | Get a single snippet |
| `PUT /api/snippets/{id}` | Update a snippet |
| `DELETE /api/snippets/{id}` | Delete a snippet |

### Submissions

| Method & Path | Description |
|---------------|-------------|
| `GET /api/submissions` | List the current user's execution history |
| `GET /api/submissions/{id}` | Get a single execution result |

### Error format

All errors return a consistent body:

```json
{ "timestamp": "2026-05-15T10:00:00Z", "status": 401, "error": "Unauthorized", "message": "Invalid or expired token" }
```

| Code | Meaning |
|------|---------|
| 400 | Validation failed / unsupported language |
| 401 | Missing or invalid JWT |
| 403 | Accessing another user's resource |
| 429 | Rate limit exceeded |
| 500 | Internal / Docker engine error |

---

## Security Measures

Untrusted code is treated as hostile. Each execution container is launched with:

- `--network none` — **no network access** at all.
- `--memory` / `--memory-swap` — memory capped (default 256 MB), swap disabled.
- `--cpus` — CPU share capped (default 0.5).
- `--pids-limit` — fork-bomb protection (process count capped).
- `--read-only` root filesystem with a small `tmpfs` for scratch files.
- `--cap-drop ALL` — all Linux capabilities removed.
- `--security-opt no-new-privileges` — privilege escalation blocked.
- Runs as a **non-root user** inside the container.
- `--rm` — the container is destroyed immediately after the run.
- A hard wall-clock timeout; the container is force-killed on overrun.

Platform-level protections:

- **JWT authentication** on every non-auth endpoint.
- **Authorization checks** so users can only touch their own data.
- **Per-user rate limiting** (HTTP 429 when exceeded).
- **Input validation** on request size, language, and payload length.
- **BCrypt** password hashing.
- Secrets supplied via environment variables, never committed.

---

## Design Decisions

- **Docker per execution, not a shared sandbox.** A fresh, throwaway container
  per run gives strong isolation with no state leaking between submissions.
  The trade-off is container startup latency, which we accept for safety.
- **Bounded thread pool + queue for concurrency.** Rather than spawning a
  container per request unbounded, executions flow through a fixed-size pool.
  This protects the host from resource exhaustion and gives predictable
  throughput under load.
- **MySQL with JPA.** The data model (users, snippets, submissions) is
  relational and benefits from constraints and joins; JPA keeps the persistence
  layer clean and portable.
- **JWT over server-side sessions.** Stateless tokens let the API scale
  horizontally without sticky sessions or a shared session store.
- **Config-driven language support.** Each language is one entry describing its
  Docker image, compile command, and run command — adding a language requires
  no new code paths.
- **Separation of concerns.** Controllers handle HTTP, services hold business
  logic, the `DockerRunner` isolates all container interaction, and
  repositories handle persistence — making each layer independently testable.

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `Cannot connect to the Docker daemon` | Ensure Docker is running and the socket is mounted into the backend container. |
| Backend exits with DB connection refused | MySQL is still starting; Compose retries via `depends_on` healthcheck. Wait and re-check. |
| First execution is slow | Language images are pulled on first use. Pre-pull them or wait once. |
| Port already in use | Change the host port mapping in `docker-compose.yml`. |
| `429 Too Many Requests` | You hit the rate limit; wait a minute or raise `RATE_LIMIT_PER_MINUTE`. |

---

## License

Released under the MIT License. Built as a Backend Engineering Launchpad case study.

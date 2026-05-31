# CodeRank — Online Code Execution Platform

CodeRank is a full-stack online code execution and coding-practice platform. Users
register, write code in the browser, run it against a sandboxed Docker container,
solve coding questions graded against test cases, and review their execution and
solution history. Administrators can author coding questions with sample and hidden
test cases.

> **Stack:** Java 17 · Spring Boot 3.3 · MySQL 8 · Docker · React 18 · Vite 5

---

## Project Overview

CodeRank consists of two applications in one repository:

- **SpringBootBackend** — a Spring Boot REST API that handles authentication (JWT),
  runs submitted code inside isolated Docker containers, grades solutions against a
  question's test cases, stores submissions/snippets/solutions in MySQL, and exposes
  OpenAPI/Swagger documentation.
- **ReactJSFrontEnd** — a React (Vite) single-page application that lets users log in,
  edit and run code in a Monaco editor, browse and solve questions, view their past
  solutions, check backend health, and (for admins) author new questions.

Every code run is executed in a throwaway Docker container with no network, capped
memory/CPU/PIDs, a read-only filesystem, dropped capabilities, and a non-root user,
so untrusted code never reaches the host. Code execution is rate-limited per user and
queued through a bounded thread pool.

---

## Technologies Used

### Backend

- **Java 17**
- **Spring Boot 3.3.4**
  - Spring Boot Starter Web
  - Spring Boot Starter Data JPA
  - Spring Boot Starter Security
  - Spring Boot Starter Validation
- **MySQL 8** (`mysql-connector-j`) — relational database
- **JWT authentication** via `io.jsonwebtoken` (jjwt 0.12.6) + Spring Security, with
  **BCrypt** password hashing and stateless sessions
- **springdoc-openapi** 2.6.0 (`springdoc-openapi-starter-webmvc-ui`) — Swagger UI / OpenAPI docs
- **Lombok** — boilerplate reduction
- **Docker** — sandboxed per-run code execution (host Docker engine)
- **Maven** — build tool (Maven Wrapper `mvnw` included)
- Test: `spring-boot-starter-test`, `spring-security-test`

### Frontend

- **React 18** (`react`, `react-dom` 18.3.1)
- **JavaScript** (ES modules, JSX)
- **Vite 5** (`@vitejs/plugin-react`) — build tool / dev server
- **React Router DOM 6** (`react-router-dom` 6.27.0) — client-side routing
- **Axios 1.7** — HTTP client with JWT request/response interceptors
- **@monaco-editor/react** 4.6.0 — in-browser code editor
- **lucide-react** 0.453.0 — icons
- Styling: hand-written CSS in `src/styles/global.css`

---

## Project Structure

```
coder_rank_project/
├── README.md
├── CodeRank_Documentation.pdf
├── docker-compose.yml
│
├── SpringBootBackend/
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── .env.example
│   ├── pom.xml
│   ├── lombok.config
│   ├── mvnw / mvnw.cmd
│   ├── .mvn/wrapper/maven-wrapper.properties
│   └── src/main/
│       ├── java/com/code/rank/
│       │   ├── Application.java
│       │   ├── config/        AdminSeeder, CorsConfig, ExecutorConfig, SecurityConfig, SwaggerConfig
│       │   ├── controller/    AdminQuestionController, AuthController, ExecutionController,
│       │   │                  HealthController, QuestionController, SnippetController,
│       │   │                  SolutionController, SubmissionController
│       │   ├── dto/
│       │   │   ├── request/   ExecuteRequest, LoginRequest, QuestionRequest, RegisterRequest,
│       │   │   │              SnippetRequest, SolutionRequest, TestCaseRequest
│       │   │   └── response/  ApiResponse, AuthResponse, ErrorResponse, ExecuteResponse,
│       │   │                  QuestionResponse, QuestionSummaryResponse, SnippetResponse,
│       │   │                  SolutionResponse, SubmissionResponse, TestCaseResponse,
│       │   │                  TestCaseResultResponse
│       │   ├── entity/        Difficulty, ExecutionStatus, Language, Question, Role, Snippet,
│       │   │                  Solution, SolutionStatus, Submission, TestCase, User
│       │   ├── exception/     BadRequestException, ExecutionFailedException, ForbiddenException,
│       │   │                  GlobalExceptionHandler, RateLimitException, ResourceNotFoundException
│       │   ├── executor/      DockerExecutor, ExecutionResult, LanguageSpec, StreamGobbler
│       │   ├── repository/    QuestionRepository, SnippetRepository, SolutionRepository,
│       │   │                  SubmissionRepository, TestCaseRepository, UserRepository
│       │   ├── security/      CustomUserDetails, JwtAuthenticationFilter, JwtTokenProvider,
│       │   │                  RestAuthenticationEntryPoint, UserDetailsServiceImpl
│       │   └── service/       AdminQuestionService, AuthService, ExecutionService,
│       │                      QuestionService, RateLimiterService, SnippetService,
│       │                      SolutionService, SubmissionService
│       └── resources/
│           └── application.properties
│
└── ReactJSFrontEnd/
    ├── index.html
    ├── package.json
    ├── package-lock.json
    ├── vite.config.js
    ├── .env.example
    ├── README.md
    └── src/
        ├── main.jsx
        ├── App.jsx
        ├── api/
        │   ├── axiosClient.js
        │   ├── authApi.js
        │   ├── executionApi.js
        │   ├── questionApi.js
        │   └── healthApi.js
        ├── components/
        │   ├── Navbar.jsx
        │   ├── ProtectedRoute.jsx
        │   ├── Loading.jsx
        │   ├── ErrorMessage.jsx
        │   └── OutputPanel.jsx
        ├── context/
        │   └── AuthContext.jsx
        ├── pages/
        │   ├── Login.jsx
        │   ├── Register.jsx
        │   ├── Dashboard.jsx
        │   ├── CodeEditor.jsx
        │   ├── Questions.jsx
        │   ├── QuestionDetail.jsx
        │   ├── Solutions.jsx
        │   ├── AdminQuestionForm.jsx
        │   ├── Health.jsx
        │   └── NotFound.jsx
        └── styles/
            └── global.css
```

---

## Features

- **User registration & login** with JWT-based authentication and BCrypt password hashing.
- **Role-based access** — `USER` and `ADMIN` roles; admin-only question authoring.
- **One-off code execution** — run code in **Python**, **Java**, or **JavaScript** with
  optional stdin, returning stdout, stderr, exit code, status, and execution time.
- **Sandboxed execution** — every run uses a throwaway Docker container (`--network none`,
  capped memory/CPU/PIDs, read-only FS, dropped capabilities, non-root user).
- **Per-user rate limiting** on execution endpoints (HTTP 429 when exceeded).
- **Bounded execution thread pool + queue** (HTTP 503 when the queue is saturated).
- **Coding questions** — browse a paged list of published questions and view details
  (sample test cases visible; hidden test cases counted but not exposed).
- **Solution grading** — submit a solution that is run against all test cases (samples +
  hidden); per-case results returned for samples, hidden cases aggregated as passed/total counts.
- **Solution history** — list past grading attempts, optionally filtered by question.
- **Submission history** — every execution is persisted and retrievable.
- **Personal snippets** — create, list, view, and delete code snippets owned by the caller.
- **Admin question management** — create, view, update, and delete questions with up to
  5 sample and 100 hidden test cases.
- **Health endpoint** for liveness checks.
- **OpenAPI / Swagger UI** documentation with Bearer-token authorization.
- **React SPA** with a Monaco code editor, protected routes, and JWT persisted to `localStorage`.

---

## Backend Setup

### Prerequisites

- JDK 17
- Maven 3.9+ (or use the included Maven Wrapper `./mvnw`)
- MySQL 8 (running and reachable)
- Docker (the backend launches sandbox containers via the Docker engine)

### Database configuration

Configuration lives in `SpringBootBackend/src/main/resources/application.properties`
and is overridable via environment variables (see `SpringBootBackend/.env.example`).

Defaults:

| Property | Default |
|----------|---------|
| `DB_URL` | `jdbc:mysql://localhost:3306/coder_rank?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `root@1234` (masked — set your own) |
| `spring.jpa.hibernate.ddl-auto` | `update` (schema auto-created/updated) |

The database `coder_rank` is created automatically thanks to
`createDatabaseIfNotExist=true`, and Hibernate creates/updates the tables on startup.

### Run the backend

```bash
cd SpringBootBackend
mvn clean install
mvn spring-boot:run
```

Or with the Maven Wrapper:

```bash
cd SpringBootBackend
./mvnw spring-boot:run
```

### Backend URL / port

- API base: **http://localhost:8080** (port from `server.port`, default `8080`)
- API endpoints are served under `/api`
- Swagger UI: **http://localhost:8080/swagger-ui/index.html**
- OpenAPI JSON: **http://localhost:8080/v3/api-docs**

> An admin account is auto-seeded on first start **only if** no admin exists **and**
> `ADMIN_PASSWORD` is set (defaults: username `admin`, email `admin@coderank.local`).

---

## Frontend Setup

### Prerequisites

- Node.js + npm
- Backend running (default `http://localhost:8080`)

### Install and run

```bash
cd ReactJSFrontEnd
npm install
npm run dev
```

The dev server prints the local URL. Per `vite.config.js`, the frontend runs on
**http://localhost:5173**.

Available scripts (from `package.json`):

- `npm run dev` — start the Vite dev server
- `npm run build` — production build (outputs to `dist/`)
- `npm run preview` — preview the production build

### Frontend configuration

The API base URL is read from `VITE_API_BASE_URL` (see `.env.example`), defaulting to
`http://localhost:8080`:

```
VITE_API_BASE_URL=http://localhost:8080
```

---

## API Endpoints

Base URL: `http://localhost:8080`. All endpoints are under `/api`.

**Public** (no JWT): `/api/auth/**`, `/api/health`, Swagger/OpenAPI paths.
**Admin only** (`ROLE_ADMIN`): `/api/admin/**`.
**All other endpoints** require `Authorization: Bearer <token>`.

### Auth

| Method | URL | Auth | Purpose | Request Body | Response |
|--------|-----|------|---------|--------------|----------|
| POST | `/api/auth/register` | Public | Register a new user (role USER), returns JWT | `{ username, email, password }` | `ApiResponse<AuthResponse>` — `{ token, tokenType, expiresInMs, userId, username }` |
| POST | `/api/auth/login` | Public | Login with credentials, returns JWT | `{ username, password }` | `ApiResponse<AuthResponse>` |

### Execution

| Method | URL | Auth | Purpose | Request Body | Response |
|--------|-----|------|---------|--------------|----------|
| POST | `/api/execute` | JWT | Run a single code snippet in a sandbox container (rate-limited) | `{ language, code, stdin?, snippetId? }` | `ApiResponse<ExecuteResponse>` — `{ submissionId, language, status, stdout, stderr, exitCode, executionTimeMs }` |

`language` ∈ `PYTHON`, `JAVA`, `JAVASCRIPT`.

### Questions

| Method | URL | Auth | Purpose | Request Body | Response |
|--------|-----|------|---------|--------------|----------|
| GET | `/api/questions` | JWT | List published questions (paged) | — (query: `page`, `size`, `sort`) | `ApiResponse<Page<QuestionSummaryResponse>>` |
| GET | `/api/questions/{id}` | JWT | View a question (sample test cases only) | — | `ApiResponse<QuestionResponse>` |
| POST | `/api/questions/{id}/solve` | JWT | Submit a solution; runs against all test cases (rate-limited) | `{ language, code }` | `ApiResponse<SolutionResponse>` — status, passedCount, totalCount, sampleResults, hiddenPassedCount, hiddenTotalCount |

### Solutions

| Method | URL | Auth | Purpose | Request Body | Response |
|--------|-----|------|---------|--------------|----------|
| GET | `/api/solutions` | JWT | List the caller's solution attempts | — (query: `questionId?`, `page`, `size`, `sort`) | `ApiResponse<Page<SolutionResponse>>` |

### Snippets

| Method | URL | Auth | Purpose | Request Body | Response |
|--------|-----|------|---------|--------------|----------|
| POST | `/api/snippets` | JWT | Create a snippet owned by the caller | `{ title, language, code }` | `ApiResponse<SnippetResponse>` |
| GET | `/api/snippets` | JWT | List the caller's snippets (paged) | — (query: `page`, `size`, `sort`) | `ApiResponse<Page<SnippetResponse>>` |
| GET | `/api/snippets/{id}` | JWT | Get a snippet (owner only) | — | `ApiResponse<SnippetResponse>` |
| DELETE | `/api/snippets/{id}` | JWT | Delete a snippet (owner only) | — | `ApiResponse<Void>` |

### Submissions

| Method | URL | Auth | Purpose | Request Body | Response |
|--------|-----|------|---------|--------------|----------|
| GET | `/api/submissions` | JWT | List the caller's submissions (paged) | — (query: `page`, `size`, `sort`) | `ApiResponse<Page<SubmissionResponse>>` |
| GET | `/api/submissions/{id}` | JWT | Get a submission (owner only) | — | `ApiResponse<SubmissionResponse>` |

### Admin — Questions

| Method | URL | Auth | Purpose | Request Body | Response |
|--------|-----|------|---------|--------------|----------|
| POST | `/api/admin/questions` | ADMIN | Create questions with test cases | `[ { title, description, difficulty, testCases:[{ input?, expectedOutput, sample }] } ]` | `ApiResponse<List<QuestionResponse>>` |
| GET | `/api/admin/questions/{id}` | ADMIN | Get a question with all test cases | — | `ApiResponse<QuestionResponse>` |
| PUT | `/api/admin/questions/{id}` | ADMIN | Replace a question and all its test cases | `{ title, description, difficulty, testCases:[...] }` | `ApiResponse<QuestionResponse>` |
| DELETE | `/api/admin/questions/{id}` | ADMIN | Delete a question and its test cases | — | `ApiResponse<Void>` |

Constraints: at least one test case; at most 5 with `sample=true` and at most 100 with `sample=false`.

### Health

| Method | URL | Auth | Purpose | Response |
|--------|-----|------|---------|----------|
| GET | `/api/health` | Public | Liveness probe | `ApiResponse<Map>` — `{ "status": "UP" }` |

> All successful responses are wrapped in a uniform envelope:
> `{ success, message, data, timestamp }`. Errors return an `ErrorResponse`
> (`{ success, status, error, message, path, timestamp }`, with `details` for validation errors).

---

## Database Configuration

- **Database:** MySQL 8
- **Driver:** `com.mysql.cj.jdbc.Driver`
- **Schema name:** `coder_rank` (auto-created via `createDatabaseIfNotExist=true`)
- **Schema management:** `spring.jpa.hibernate.ddl-auto=update` (tables created/updated on startup)
- **SQL logging:** `spring.jpa.show-sql=true`, formatted SQL enabled

Connection settings come from `application.properties` and are overridable through
environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`). The repository ships a
default local password in `application.properties` for local development only —
**set your own credentials** via environment variables or `.env` and never commit real
secrets. JPA entities persisted: `User`, `Question`, `TestCase`, `Snippet`, `Submission`, `Solution`.

---

## Docker Setup

A `docker-compose.yml` at the project root runs **MySQL 8** and the **Spring Boot backend** together.

```bash
docker-compose up --build
```

This starts:

| Service | Container | URL / Port |
|---------|-----------|-----------|
| mysql | `coderank-mysql` | `localhost:3306` (database `coder_rank`) |
| backend | `coderank-backend` | `http://localhost:8080` |

Notes from the compose file:

- The backend waits for MySQL to be healthy before starting (`depends_on` + healthcheck).
- The backend mounts the host Docker socket (`/var/run/docker.sock`) so it can launch
  per-run sandbox containers, and `/tmp/coderank` as a scratch directory.
- Credentials and secrets (`DB_PASSWORD`, `JWT_SECRET`, `ADMIN_PASSWORD`, etc.) are
  configurable via environment variables; replace the defaults for any non-local use.

> **Security note (from the compose file):** mounting the Docker socket grants the
> backend root-equivalent access to the host. Only run this in trusted environments.

The frontend is **not** part of `docker-compose.yml`; run it separately (see Frontend Setup).

---

## Screens / Pages

Frontend routes are defined in `ReactJSFrontEnd/src/App.jsx`:

| Path | Page | Access |
|------|------|--------|
| `/login` | Login | Public |
| `/register` | Register | Public |
| `/` | Dashboard | Authenticated |
| `/editor` | Code Editor (Monaco, run code) | Authenticated |
| `/questions` | Browse questions | Authenticated |
| `/questions/:id` | Question detail + submit solution | Authenticated |
| `/solutions` | Your solution attempts | Authenticated |
| `/admin/questions/new` | Author a new question | Admin only |
| `/health` | Backend health probe | Authenticated |
| `*` | Not Found (404) | — |

Shared components: `Navbar`, `ProtectedRoute`, `Loading`, `ErrorMessage`, `OutputPanel`.
Auth state (token + user, role read from the JWT) is managed in `context/AuthContext.jsx`
and persisted to `localStorage` (`cr_token`, `cr_user`).

---

## How to Run Complete Project

### Option A — Docker Compose for backend + MySQL, npm for frontend

1. **Start MySQL + backend:**
   ```bash
   docker-compose up --build
   ```
   Backend at `http://localhost:8080`, MySQL at `localhost:3306`.

2. **Start the frontend:**
   ```bash
   cd ReactJSFrontEnd
   npm install
   npm run dev
   ```
   Frontend at `http://localhost:5173`.

3. Open `http://localhost:5173`, register or log in, and use the app.

### Option B — Run everything locally

1. **Start MySQL 8** locally and make sure it matches `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`.

2. **Start the backend:**
   ```bash
   cd SpringBootBackend
   mvn spring-boot:run
   ```
   Backend at `http://localhost:8080`.

3. **Start the frontend:**
   ```bash
   cd ReactJSFrontEnd
   npm install
   npm run dev
   ```
   Frontend at `http://localhost:5173`.

4. The frontend reads the backend URL from `VITE_API_BASE_URL` (default
   `http://localhost:8080`). The backend allows CORS from `http://localhost:5173`
   and `http://localhost:3000` by default.

> Docker must be running for code execution (the backend spawns sandbox containers).
> On first run, the language images (`python:3.11-slim`, `eclipse-temurin:17-jdk-jammy`,
> `node:18-alpine`) are pulled, so the first execution may be slow.

---

## Submission Notes

This project, **CodeRank**, is a full-stack online code execution platform built with a
**Spring Boot (Java 17) + MySQL** backend and a **React (Vite)** frontend. It demonstrates
JWT-based authentication and role-based authorization, secure sandboxed code execution
inside Docker containers, automated grading of coding questions against sample and hidden
test cases, rate limiting, a bounded execution thread pool, RESTful API design with a
uniform response envelope, and OpenAPI/Swagger documentation. The frontend provides a
Monaco-based code editor, protected routing, and persisted JWT auth.

All instructions above are derived directly from the project's source files
(`pom.xml`, `package.json`, `application.properties`, `docker-compose.yml`, controllers,
and React routes/components).

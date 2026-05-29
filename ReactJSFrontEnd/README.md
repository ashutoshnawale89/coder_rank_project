# CodeRank — React Frontend

The browser client for the CodeRank online code execution platform. It talks to
the Spring Boot backend in `../SpringBootBackend`.

## Tech Stack

| Layer | Tech |
|-------|------|
| Build | Vite 5 |
| UI    | React 18 + React Router 6 |
| HTTP  | Axios (central client + interceptors) |
| Editor| `@monaco-editor/react` (VS Code editor in the browser) |
| Icons | `lucide-react` |
| Auth  | JWT (Bearer) persisted to `localStorage` |
| Styles| Hand-rolled modern CSS in `src/styles/global.css` |

## Folder Structure

```
ReactJSFrontEnd/
├── index.html
├── package.json
├── vite.config.js
├── .env.example
├── .env
└── src/
    ├── api/
    │   ├── axiosClient.js     # central client, JWT interceptor, response unwrap
    │   ├── authApi.js         # /api/auth/register, /api/auth/login
    │   ├── executionApi.js    # /api/execute
    │   ├── snippetApi.js      # /api/snippets ...
    │   ├── submissionApi.js   # /api/submissions ...
    │   ├── questionApi.js     # /api/questions, /api/solutions, /api/admin/questions
    │   └── healthApi.js       # /api/health
    ├── components/
    │   ├── Navbar.jsx
    │   ├── ProtectedRoute.jsx
    │   ├── Loading.jsx
    │   ├── ErrorMessage.jsx
    │   └── OutputPanel.jsx
    ├── context/
    │   └── AuthContext.jsx    # token + user state, login/register/logout
    ├── pages/
    │   ├── Login.jsx
    │   ├── Register.jsx
    │   ├── Dashboard.jsx
    │   ├── CodeEditor.jsx
    │   ├── Snippets.jsx
    │   ├── SnippetDetail.jsx
    │   ├── Submissions.jsx
    │   ├── SubmissionDetail.jsx
    │   ├── Questions.jsx
    │   ├── QuestionDetail.jsx
    │   ├── Solutions.jsx
    │   ├── AdminQuestionForm.jsx
    │   ├── Health.jsx
    │   └── NotFound.jsx
    ├── styles/global.css
    ├── App.jsx
    └── main.jsx
```

## Setup

```bash
cd ReactJSFrontEnd
npm install
cp .env.example .env       # adjust if backend isn't on localhost:8080
npm run dev
```

Vite serves the app at **http://localhost:5173**.

## Connecting to the backend

The Spring Boot backend must be running on the URL configured in `VITE_API_BASE_URL`.

1. Start MySQL and the backend (`./mvnw spring-boot:run` from `SpringBootBackend/`).
2. The backend already allows CORS from `http://localhost:5173` (see
   `SpringBootBackend/src/main/java/com/code/rank/config/CorsConfig.java`).
3. Set `ADMIN_PASSWORD` in the backend `.env` if you want the auto-seeded admin
   user so you can author questions from the UI.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_BASE_URL` | `http://localhost:8080` | Base URL for backend API calls |

Anything that should reach the browser **must** be prefixed with `VITE_`.

## Frontend Routes

| Path | Page | Access |
|------|------|--------|
| `/login` | Login | Public (redirects authenticated users to `/`) |
| `/register` | Register | Public (redirects authenticated users to `/`) |
| `/` | Dashboard | Authenticated |
| `/editor` | Code Editor | Authenticated |
| `/snippets` | Snippet list + create | Authenticated |
| `/snippets/:id` | Snippet detail / run / delete | Authenticated |
| `/submissions` | Execution history list | Authenticated |
| `/submissions/:id` | Submission detail | Authenticated |
| `/questions` | Browse questions | Authenticated |
| `/questions/:id` | Question detail + submit solution | Authenticated |
| `/solutions` | Your solution attempts | Authenticated |
| `/admin/questions/new` | Author a new question | Admin only |
| `/health` | Backend health probe | Authenticated |
| `*` | 404 | — |

## Backend APIs used

| Method | Path | Wrapper |
|--------|------|---------|
| POST   | `/api/auth/register`            | `authApi.register` |
| POST   | `/api/auth/login`               | `authApi.login` |
| POST   | `/api/execute`                  | `executionApi.execute` |
| POST   | `/api/snippets`                 | `snippetApi.create` |
| GET    | `/api/snippets`                 | `snippetApi.list` |
| GET    | `/api/snippets/{id}`            | `snippetApi.get` |
| DELETE | `/api/snippets/{id}`            | `snippetApi.delete` |
| GET    | `/api/submissions`              | `submissionApi.list` |
| GET    | `/api/submissions/{id}`         | `submissionApi.get` |
| GET    | `/api/questions`                | `questionApi.list` |
| GET    | `/api/questions/{id}`           | `questionApi.get` |
| POST   | `/api/questions/{id}/solve`     | `questionApi.solve` |
| GET    | `/api/solutions`                | `solutionApi.list` |
| POST   | `/api/admin/questions`          | `adminQuestionApi.create` |
| GET    | `/api/admin/questions/{id}`     | `adminQuestionApi.get` |
| PUT    | `/api/admin/questions/{id}`     | `adminQuestionApi.update` |
| DELETE | `/api/admin/questions/{id}`     | `adminQuestionApi.delete` |
| GET    | `/api/health`                   | `healthApi.check` |

## Axios API client

`src/api/axiosClient.js` creates a single Axios instance with:

- `baseURL` from `VITE_API_BASE_URL`.
- A **request interceptor** that attaches `Authorization: Bearer <token>` when a
  JWT exists in `localStorage` under `cr_token`.
- A **response interceptor** that:
  - On HTTP 401, clears the stored token + user and redirects to `/login`.
  - Otherwise passes errors through so each page can show its own message.
- An `unwrap(response)` helper that pulls the `data` field out of the backend's
  uniform `{ success, message, data, timestamp }` envelope.
- An `extractErrorMessage(err)` helper that returns a friendly message from the
  backend's `ErrorResponse` body, including field-level validation details.

Each domain has its own thin wrapper file (e.g. `snippetApi.js`) so pages never
talk to `axios` directly.

## JWT Authentication Flow

1. **Login / Register** → `authApi.login` or `authApi.register` returns
   `{ token, tokenType, expiresInMs, userId, username }`.
2. `AuthContext` stores `token` and a small `user` object in `localStorage`
   (`cr_token`, `cr_user`). The role (`USER` / `ADMIN`) is read from the JWT
   payload.
3. On every subsequent request the Axios interceptor attaches
   `Authorization: Bearer <token>`.
4. `ProtectedRoute` wraps any page that requires auth. If `isAuthenticated`
   is false it redirects to `/login` (preserving the target path for after
   login). The `adminOnly` flag additionally requires `role === 'ADMIN'`.
5. **Logout** clears `localStorage` and routes back to `/login`.
6. If the API returns 401 (expired/invalid token) the response interceptor
   wipes state and routes to `/login` automatically.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| CORS error in browser console | Backend isn't running, isn't on `VITE_API_BASE_URL`, or `CORS_ALLOWED_ORIGINS` excludes `http://localhost:5173`. Restart backend after editing. |
| `Network Error` from Axios | Backend down or wrong URL. Check `/health` in another tab and `VITE_API_BASE_URL`. |
| Stuck redirect to `/login` | Token expired. Login again. |
| Monaco editor doesn't render | Re-run `npm install`. Vite must serve `@monaco-editor/react`. |
| 403 on `/api/admin/**` | Logged-in user is not ADMIN. Set `ADMIN_PASSWORD` in the backend `.env` and log in as that admin. |
| 429 when running code | Per-user rate limit reached; wait a minute or raise `RATE_LIMIT_RPM` on the backend. |
| 503 on `/api/execute` | Execution queue is full; backend is busy. |

## Build for production

```bash
npm run build       # outputs to dist/
npm run preview     # serves dist/ for sanity-checking the build
```

Drop `dist/` behind any static host (Nginx, Caddy, S3 + CloudFront, GitHub
Pages, etc.). Make sure the host rewrites unknown routes to `index.html` so
React Router can handle them.

# TestBuddy — AI-Powered API Test Automation

> Upload your API spec. Get intelligent tests in seconds. Understand every failure instantly.

TestBuddy turns an OpenAPI/Swagger spec into a full suite of executed, explained, and exportable API tests — powered by Claude and Gemini.

---

## The Problem

Writing API tests is tedious. Debugging why they fail is even worse. Most teams skip edge cases, miss negative scenarios, and spend hours reading raw HTTP responses trying to figure out what went wrong.

TestBuddy eliminates all three problems with a single upload.

---

## What It Does

1. **Upload** your OpenAPI 3.0+ or Swagger 2.0 spec (JSON or YAML)
2. **Generate** 12–15 intelligent test cases using Claude Haiku — covering happy path, negative, and edge case scenarios
3. **Configure** your target environment (dev / staging / prod) and authentication (JWT Bearer or API Key)
4. **Execute** all tests against your live API, with automatic request chaining between dependent endpoints
5. **Understand** every failure with a Gemini-powered "Why?" explanation: technical root cause, human-readable analogy, and an actionable fix
6. **Export** a color-coded Excel report for stakeholders

---

## Demo Flow

```
Upload spec.json
      ↓
Claude reads your API → generates realistic test cases with payloads, expected status codes, and test types
      ↓
TestBuddy executes each test against your base URL
  ├─ Injects auth headers automatically
  ├─ Chains requests (POST /users → extracts ID → GET /users/{id})
  └─ Records status, response body, and response time
      ↓
Results dashboard: pass/fail pie chart, response time bar chart, full result table
      ↓
Click "Why?" on any failure → Gemini returns:
  ├─ Technical: "Your API returned 401 because the Bearer token has expired"
  ├─ Human: "It's like trying to enter a building after your keycard was deactivated"
  └─ Suggestion: "Refresh your token or check the Authorization header format"
      ↓
Export to Excel → share with your team
```

---

## Key Features

### Intelligent Test Generation
Claude Haiku analyzes your full API spec — endpoints, parameters, schemas, and relationships — and generates test cases that actually reflect how your API is meant to be used. Users can also provide custom instructions to guide generation (e.g., "focus on auth edge cases" or "use this sample user ID").

### Automatic Request Chaining
Real APIs aren't tested in isolation. TestBuddy detects dependencies between test cases and automatically extracts resource IDs from previous responses (UUID and numeric), injecting them into subsequent requests. Create → Read → Update → Delete flows work out of the box.

### Dual-Model AI Architecture
| Model | Role |
|---|---|
| Claude Haiku 4.5 | Test case generation (10K token context, speed-optimized) |
| Gemini 2.5 Flash | Failure explanation (low temperature, precision-optimized) |

Two models, each used where it excels. Claude for creative, context-heavy generation. Gemini for concise, grounded explanations.

### Results Dashboard
- Pass/fail ratio pie chart
- Average response time per endpoint (bar chart)
- Full execution table: test name, endpoint, method, expected vs actual status, error messages, response time
- Filter by All / Passed / Failed / Not Run
- Search by test name or endpoint

### AI Insights
The Insights page surfaces all failed tests in a sidebar. Clicking any failure opens a structured Gemini analysis — no raw stack traces, no guessing. Three sections: what went wrong technically, what it means in plain English, and exactly what to fix.

### Excel Export
Styled `.xlsx` report with color-coded pass (green) / fail (coral) rows, auto-sized columns, full request/response details, and timing data. Built for sharing with people who don't read terminal output.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Browser (React)                       │
│  Upload → Generate → Run → Results → Insights → Export      │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP / Axios
┌────────────────────────▼────────────────────────────────────┐
│                   Spring Boot 3.2 (Port 8080)                │
│                                                              │
│  SpecController     → SpecParserService (Swagger Parser v3)  │
│  TestController     → ClaudeService (Anthropic API)          │
│                     → TestExecutorService (WebFlux)          │
│                     → ExcelExportService (Apache POI)        │
│  InsightController  → GeminiService (Google AI API)          │
└──────────┬──────────────────────────┬───────────────────────┘
           │ JDBC / HikariCP          │ External APIs
┌──────────▼──────────┐   ┌───────────▼──────────────────────┐
│  PostgreSQL 15       │   │  api.anthropic.com (Claude)       │
│  3 tables, JSONB     │   │  generativelanguage.googleapis.com│
│  Flyway migrations   │   └──────────────────────────────────┘
└─────────────────────┘
```

### Backend
- **Spring Boot 3.2.0** / Java 17
- **Spring WebFlux** — non-blocking reactive HTTP client for test execution
- **Swagger Parser v3** — OpenAPI spec parsing
- **Apache POI** — Excel report generation
- **Flyway** — versioned database migrations
- **PostgreSQL JSONB** — flexible storage for headers, payloads, and API responses

### Frontend
- **React 18** + **TypeScript 5.8**
- **Vite 5.4** — fast builds
- **Shadcn/ui** (Radix UI + Tailwind CSS) — accessible, composable UI
- **Zustand v5** — lightweight state with localStorage persistence
- **Recharts** — pass/fail and performance visualizations
- **React Hook Form + Zod** — type-safe form validation

### Infrastructure
- **Docker** — containerized backend and frontend
- **Docker Compose** — one-command local development
- **Kubernetes** — production-grade deployment with namespace isolation, secrets management, persistent volumes, and readiness probes

---

## Database Schema

```sql
test_runs          -- A single test session (spec upload → execution)
  id UUID PK
  spec_filename TEXT
  instructions  TEXT
  environment   TEXT        -- dev | staging | prod
  status        TEXT        -- SPEC_UPLOADED | TESTS_GENERATED | RUNNING | COMPLETED
  created_at    TIMESTAMP
  completed_at  TIMESTAMP

test_cases         -- Generated test cases for a run
  id UUID PK
  run_id UUID FK → test_runs
  name TEXT, endpoint TEXT, method TEXT
  headers JSONB, payload JSONB
  expected_status INT
  test_type TEXT             -- happy | negative | edge
  description TEXT
  chain_from UUID            -- reference to a previous test case (chaining)

test_results       -- Execution results per test case
  id UUID PK
  test_case_id UUID FK → test_cases
  run_id UUID FK → test_runs
  actual_status INT
  actual_response JSONB
  response_time_ms BIGINT
  passed BOOLEAN
  error_message TEXT
  executed_at TIMESTAMP
```

---

## API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/spec/upload` | Upload OpenAPI spec (multipart), returns parsed spec + runId |
| POST | `/api/tests/generate` | Generate test cases via Claude |
| POST | `/api/tests/run` | Execute tests against target URL |
| GET | `/api/tests/results/{runId}` | Fetch execution results |
| GET | `/api/tests/export/{runId}` | Download Excel report |
| POST | `/api/insights/explain` | Gemini explains a failed test |

---

## Running Locally

### Option 1 — Docker Compose (recommended)

```bash
git clone <repo>
cd TestBuddy

# 1. Copy the example env file and fill in your keys
cp .env.example .env
# Edit .env — add your CLAUDE_API_KEY, GEMINI_API_KEY, POSTGRES_PASSWORD

# 2. Build and start everything
docker-compose up --build
```

Frontend: `http://localhost:3000` | Backend: `http://localhost:8080`

> On subsequent runs (no code changes) you can skip `--build` and just run `docker-compose up`.

### Option 2 — Kubernetes

**Prerequisites:** Docker Desktop with Kubernetes enabled

```bash
# 1. Build and push images
.\build-and-push.ps1 -Username your-dockerhub-username

# 2. Create namespace
kubectl apply -f k8s/00-namespace.yaml

# 3. Create secrets
kubectl create secret generic testbuddy-secrets -n testbuddy \
  --from-literal=POSTGRES_PASSWORD="your-password" \
  --from-literal=CLAUDE_API_KEY="sk-ant-..." \
  --from-literal=GEMINI_API_KEY="AIza..."

# 4. Deploy
kubectl apply -f k8s/02-postgres.yaml
kubectl apply -f k8s/03-backend.yaml
kubectl apply -f k8s/04-frontend.yaml

# 5. Port-forward
kubectl port-forward svc/frontend 30000:80 -n testbuddy
kubectl port-forward svc/backend 8080:8080 -n testbuddy
```

Visit `http://localhost:30000`

---

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `CLAUDE_API_KEY` | Yes | Anthropic API key for test generation |
| `GEMINI_API_KEY` | Yes | Google AI API key for failure insights |
| `POSTGRES_PASSWORD` | Yes | PostgreSQL database password |
| `SPRING_DATASOURCE_URL` | No | DB URL (defaults to `localhost:5432/testai`) |
| `VITE_API_URL` | No | Backend URL for frontend (defaults to `http://localhost:8080`) |

---

## Project Structure

```
TestBuddy/
├── Backend/ai-api-tester/
│   ├── src/main/java/com/testai/
│   │   ├── controller/          # SpecController, TestController, InsightController
│   │   ├── service/             # ClaudeService, GeminiService, TestExecutorService
│   │   │                        # SpecParserService, ExcelExportService
│   │   ├── model/               # TestRun, TestCase, TestResult (JPA entities)
│   │   └── dto/                 # Request/response transfer objects
│   ├── src/main/resources/
│   │   ├── application.yaml
│   │   └── db/migration/        # V1__init.sql, V2__add_description_expand_fields.sql
│   └── Dockerfile
├── Frontend/
│   ├── src/
│   │   ├── pages/               # Dashboard, Upload, TestCases, Results, Insights
│   │   ├── components/          # UI components (Shadcn-based)
│   │   ├── store/               # Zustand state management
│   │   └── lib/api.ts           # Axios client
│   └── Dockerfile
├── k8s/                         # Kubernetes manifests
├── docker-compose.yml
└── build-and-push.ps1           # One-shot image build + push script
```

---

## Technical Highlights

- **Request chaining** — regex-based ID extraction from response bodies, injected into path params of dependent tests. Handles both UUID and integer ID formats.
- **Reactive execution** — Spring WebFlux WebClient for non-blocking HTTP, 30-second timeout per test.
- **JSONB columns** — headers, payloads, and API responses stored as native PostgreSQL JSON for schema flexibility and queryability.
- **Markdown stripping** — both Claude and Gemini services handle cases where the model wraps JSON in markdown fences, preventing parse failures.
- **Kubernetes secret injection** — API keys never baked into images; injected at runtime via K8s secrets.
- **Flyway versioning** — schema changes are repeatable and tracked, safe for multi-environment deployments.
- **Zustand persistence** — frontend state survives page refresh, including generated test cases and results.

---

## Built With

Claude Haiku 4.5 · Gemini 2.5 Flash · Spring Boot 3.2 · React 18 · PostgreSQL 15 · Kubernetes · Docker · Apache POI · Swagger Parser · Tailwind CSS · Recharts

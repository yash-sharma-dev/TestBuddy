# TestBuddy — Code Audit Report
**Audit ID:** audit-1
**Date:** 2026-04-27
**Auditor:** Senior Software Architect (Claude Code)
**Branch audited:** `test-cases-for-token-optemizations`
**Scope:** Full-stack audit — Backend (Spring Boot 3.2 / Java), Frontend (React 18 / TypeScript), Infrastructure (Docker, Kubernetes)
**Purpose:** Transition from prototype phase to collaborative, production-ready codebase

---

## Executive Summary

TestBuddy is a full-stack AI-powered API test automation platform. The codebase demonstrates a solid architectural foundation — clear layering, modern technology choices, and thoughtful feature design. However, a set of critical security and stability gaps must be resolved before this system can be considered production-ready.

**Overall Score: 7 / 10**

| Dimension | Score | Notes |
|-----------|-------|-------|
| Architecture | 7/10 | Good layering; controllers too fat, no mapper layer |
| Security | 3/10 | No auth, CORS wildcard, no rate limiting |
| Code Quality | 6/10 | Duplication in controller, JSONB type confusion |
| Observability | 2/10 | Plain-text logs, no metrics, no tracing |
| Scalability | 5/10 | Sequential execution, no pagination, no caching |
| Test Coverage | 1/10 | <5% — one dummy test each side |
| Deployment Readiness | 4/10 | Docker/K8s present but HA gaps, 1 replica, no migrations |

---

## 1. Project Overview

### Technology Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2, Java, Spring Data JPA, WebClient (Reactor) |
| Database | PostgreSQL 15, JSONB columns, Flyway (disabled) |
| AI | Anthropic Claude (test generation + failure analysis) |
| Frontend | React 18, TypeScript, Vite 5.4, Tailwind CSS, Shadcn/ui |
| State | Zustand + localStorage persistence |
| Server State | React Query 5 |
| HTTP Client | Axios |
| Charts | Recharts |
| Forms | React Hook Form + Zod |
| Containers | Docker Compose (local), Kubernetes (production) |

### Estimated Codebase Size

| Component | Files | Approx. Lines |
|-----------|-------|--------------|
| Backend Java | 28 | ~2,500 |
| Frontend TSX/TS | 100+ | ~5,500 |
| Config / Infra | 10 | ~400 |
| **Total** | **~138** | **~8,400** |

---

## 2. Directory Structure

### Current Layout

```
TestBuddy/
├── Backend/ai-api-tester/
│   └── src/main/java/com/testai/ai_api_tester/
│       ├── config/          (WebClientConfig)
│       ├── controller/      (Spec, Test, Insight, Auth)
│       ├── service/         (Claude, TestExecutor, Excel, SpecParser)
│       ├── model/           (TestRun, TestCase, TestResult, TokenUsageLog, User)
│       ├── repository/      (5 Spring Data JPA repos)
│       └── dto/             (flat — request/response mixed)
│
├── Frontend/src/
│   ├── pages/               (11 pages)
│   ├── components/          (layout/, testbuddy/, ui/)
│   ├── lib/                 (api.ts — monolithic HTTP client)
│   ├── store/               (appStore.ts — Zustand)
│   ├── hooks/               (use-mobile, use-toast)
│   └── data/                (mock.ts — static placeholder data)
│
├── k8s/                     (namespace, secrets, postgres, backend, frontend)
├── docker-compose.yml
└── README.md
```

### Recommended Layout (post-refactor)

```
TestBuddy/
├── Backend/ai-api-tester/
│   └── src/main/java/com/testai/ai_api_tester/
│       ├── config/
│       │   ├── WebClientConfig.java
│       │   ├── SecurityConfig.java           ← NEW: JWT + CORS whitelist
│       │   └── CacheConfig.java              ← NEW: Spring Cache
│       ├── controller/
│       │   ├── SpecController.java
│       │   ├── TestController.java           ← slimmed (delegates to service)
│       │   ├── InsightController.java
│       │   ├── AuthController.java
│       │   └── advice/
│       │       └── GlobalExceptionHandler.java  ← NEW: @ControllerAdvice
│       ├── service/
│       │   ├── ClaudeService.java
│       │   ├── TestExecutorService.java
│       │   ├── TestOrchestrationService.java ← NEW: business logic from controller
│       │   ├── ExcelExportService.java
│       │   └── SpecParserService.java
│       ├── mapper/
│       │   └── TestResultMapper.java         ← NEW: eliminates duplicated mapping
│       ├── model/
│       ├── repository/
│       ├── dto/
│       │   ├── request/                      ← NEW: separated by direction
│       │   └── response/
│       └── properties/
│           └── ClaudeProperties.java         ← NEW: typed config (pricing, model)
│
└── Frontend/src/
    ├── api/                                  ← NEW: split from monolithic api.ts
    │   ├── client.ts                         (Axios instance + interceptors)
    │   ├── specs.api.ts
    │   ├── tests.api.ts
    │   └── insights.api.ts
    ├── types/                                ← NEW: consolidated type definitions
    │   ├── api.types.ts
    │   └── store.types.ts
    ├── constants/                            ← NEW: no more magic strings
    │   ├── auth.ts
    │   └── environment.ts
    ├── hooks/
    │   ├── use-mobile.tsx
    │   ├── use-toast.ts
    │   ├── useTestRun.ts                     ← NEW: React Query hooks
    │   └── useInsight.ts                     ← NEW
    ├── pages/
    ├── components/
    ├── store/
    └── utils/
        └── download.ts                       ← NEW: extracted blob download logic
```

---

## 3. Findings

### 3.1 Critical Issues (Must Fix Before Production)

---

#### AUDIT-C01 — No Real Authentication or Authorization

**Severity:** Critical
**Files:** All controllers, `Frontend/src/store/appStore.ts`, `Frontend/src/components/layout/ProtectedOutlet.tsx`

**Observation:**
Every backend endpoint is publicly accessible — no JWT validation, no session check. The frontend has a `ProtectedOutlet` component and a `isAuthenticated` flag in Zustand, but these are wired to a mock session (`userEmail: "jane@testbuddy.dev"`). Sign-up and sign-in pages exist in the UI but do not communicate with the backend.

**Risk:**
Any external actor can call `/api/tests/generate` and consume Claude API credits. No data isolation exists — one user can read another's test runs.

**Remediation:**
1. Backend: implement `POST /auth/login` and `POST /auth/signup` returning a signed JWT.
2. Backend: add `SecurityConfig.java` with a `JwtAuthenticationFilter`, protect all `/api/**` routes.
3. Frontend: store JWT in `appStore` after login, attach via Axios request interceptor.
4. Add `@PreAuthorize` on controller methods; filter queries by `@CurrentUser`.

---

#### AUDIT-C02 — CORS Wildcard on All Controllers

**Severity:** Critical
**Files:** `TestController.java:25`, `SpecController.java`, `InsightController.java`, `AuthController.java`

**Observation:**
`@CrossOrigin(origins = "*")` is applied to every controller, allowing any browser origin to make credentialed requests to the API.

**Remediation:**
Replace with a single global `CorsConfigurationSource` bean in `SecurityConfig.java`, setting `allowedOrigins` from an `app.cors.allowed-origins` config property (e.g., `http://localhost:3000` in dev, `https://yourdomain.com` in prod).

---

#### AUDIT-C03 — No Database Migrations (Flyway Disabled)

**Severity:** Critical
**Files:** `Backend/ai-api-tester/src/main/resources/application.yaml`

**Observation:**
`flyway.enabled: false` and `spring.jpa.hibernate.ddl-auto: update`. Schema changes are applied silently by Hibernate at startup with no version tracking, making rollbacks impossible and causing unpredictable behaviour in team environments.

**Remediation:**
1. Set `flyway.enabled: true` and `ddl-auto: validate`.
2. Write `V1__init.sql` capturing the current schema.
3. All future schema changes go through numbered migration scripts (`V2__add_user_id.sql`, etc.).

---

#### AUDIT-C04 — TypeScript Strict Mode Disabled

**Severity:** Critical
**Files:** `Frontend/tsconfig.json`

**Observation:**
```json
"noImplicitAny": false,
"strictNullChecks": false,
"noUnusedLocals": false
```
TypeScript is operating in loose mode. Implicit `any`, unchecked nulls, and unused variables are all permitted, which defeats the primary value of TypeScript for a collaborative codebase.

**Remediation:**
Set `"strict": true` in `tsconfig.json`. Fix emerging type errors incrementally — prioritise pages and API layer first.

---

#### AUDIT-C05 — No Global Exception Handler

**Severity:** Critical
**Files:** `TestController.java`, `SpecController.java`, `InsightController.java`

**Observation:**
Every controller method wraps its body in a `try/catch` block and returns `ApiResponse.error(e.getMessage())`. This pattern:
- Leaks internal exception messages (including stack details) to API consumers.
- Produces inconsistent HTTP status codes (all failures return 200 with `success: false`).
- Requires duplication in every new endpoint.

**Remediation:**
Create `GlobalExceptionHandler.java` annotated with `@RestControllerAdvice`. Map `EntityNotFoundException` → 404, `MethodArgumentNotValidException` → 400, uncaught `Exception` → 500 (with message redacted). Remove all try/catch blocks from controllers.

---

### 3.2 High-Impact Issues

---

#### AUDIT-H01 — Duplicated DTO Mapping in TestController

**Severity:** High
**Files:** `TestController.java:152–172` and `TestController.java:208–225`

**Observation:**
The `TestResult` entity → `TestResultDto` mapping is copy-pasted verbatim between `getResults()` and `exportResults()`. Any change to the mapping (e.g., adding a new field) must be applied in two places.

**Remediation:**
Extract to `TestResultMapper.java` (a `@Component`). Both methods call `testResultMapper.toDto(result, testCase)`. See refactored code in Part 3 of the main audit.

---

#### AUDIT-H02 — Repositories Injected Directly into Controller

**Severity:** High
**Files:** `TestController.java:29–36`

**Observation:**
`TestController` directly injects `TestRunRepository`, `TestCaseRepository`, `TestResultRepository`, and `ObjectMapper`. Business logic (status transitions, summary calculation) lives inside HTTP handler methods. This violates the Controller → Service → Repository layering.

**Remediation:**
Create `TestOrchestrationService.java`. Move all business logic there. The controller retains only HTTP concerns (parameter binding, header setting, response codes). See refactored code in Part 3 of the main audit.

---

#### AUDIT-H03 — Hardcoded Claude API Pricing

**Severity:** High
**Files:** `ClaudeService.java:260–264`

**Observation:**
```java
BigDecimal inputCost = BigDecimal.valueOf(inputTokens).multiply(new BigDecimal("0.000001"));
BigDecimal cacheReadCost = BigDecimal.valueOf(cacheReadTokens).multiply(new BigDecimal("0.0000001"));
BigDecimal cacheCreationCost = BigDecimal.valueOf(cacheCreationTokens).multiply(new BigDecimal("0.00000125"));
BigDecimal outputCost = BigDecimal.valueOf(outputTokens).multiply(new BigDecimal("0.000005"));
```
If Anthropic changes pricing, token cost logs silently become incorrect. No model-to-pricing mapping exists.

**Remediation:**
Create `ClaudeProperties.java` (`@ConfigurationProperties(prefix = "claude")`). Add fields `pricing.inputPerMillion`, `pricing.outputPerMillion`, `pricing.cacheReadPerMillion`, `pricing.cacheCreationPerMillion`. Configure in `application.yaml`. Inject the properties bean into `ClaudeService`.

---

#### AUDIT-H04 — Full Claude Payload Logged at INFO Level

**Severity:** High
**Files:** `ClaudeService.java:222–225`

**Observation:**
```java
log.info("Claude API Payload:\n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody));
```
In production with log aggregation (ELK, Loki, CloudWatch), this will write the complete API spec and user instructions to the log stream, potentially exposing proprietary API designs.

**Remediation:**
Change to `log.debug(...)`. Debug logs are disabled by default in production log profiles. Never log request bodies at INFO or above.

---

#### AUDIT-H05 — No Axios Timeout or Auth Interceptor

**Severity:** High
**Files:** `Frontend/src/lib/api.ts:98–101`

**Observation:**
```typescript
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
});
```
No `timeout` is set — a hanging server will block the UI indefinitely. No request interceptor attaches a JWT, meaning once auth is implemented on the backend, all frontend calls will fail with 401.

**Remediation:**
Add `timeout: 30_000` to the Axios config. Add a request interceptor that reads the token from `useAppStore.getState().authToken` and sets `Authorization: Bearer <token>`. See `client.ts` in Part 3 of the main audit.

---

#### AUDIT-H06 — No Pagination on Results Endpoint

**Severity:** High
**Files:** `TestController.java:141–197`, `TestResultRepository.java`

**Observation:**
`GET /api/tests/results/{runId}` loads the full result set into memory and returns it in a single JSON response. At 1,000+ test cases this causes heap pressure on the server and large payloads on the client.

**Remediation:**
Add `@PageableDefault(size = 50) Pageable pageable` to the handler. Return `Page<TestResultDto>`. Update frontend to fetch pages and render with pagination controls.

---

#### AUDIT-H07 — Sequential Test Execution

**Severity:** High
**Files:** `TestExecutorService.java`

**Observation:**
Test cases are executed in a sequential `for` loop. At 100 tests with an average response time of 2 seconds each, a single run takes approximately 200 seconds. Independent tests have no reason to be sequential.

**Remediation:**
For tests without `chainFrom` dependencies: execute in parallel using `Flux.fromIterable(tests).flatMap(t -> executeSingleTest(t), concurrency).collectList().block()`. Chain-dependent tests remain sequential within their dependency chain. Estimated improvement: 5–10× faster for typical API test suites.

---

#### AUDIT-H08 — Brittle Regex-Based ID Extraction

**Severity:** High
**Files:** `TestExecutorService.java`

**Observation:**
The request chaining feature extracts IDs from responses using a regex that assumes the field is named `"id"` and appears at the top level of the JSON:
```java
Pattern.compile("\"id\"\\s*:\\s*\"?([^,\"\\}]+)\"?")
```
This fails for nested IDs (`{"data": {"id": "..."}}`), differently named fields (`userId`, `resourceId`), and numeric IDs without quotes.

**Remediation:**
Add a `chainIdPath` field to `TestCaseDto` (e.g., `"data.id"`, `"userId"`). Use a JSONPath library (`com.jayway.jsonpath:json-path`) to extract the value at the specified path. Fall back to the current behaviour if no path is specified.

---

### 3.3 Moderate Issues

---

#### AUDIT-M01 — No Unit or Integration Tests

**Severity:** Medium
**Files:** `Frontend/src/test/example.test.ts`, `Backend/.../AiApiTesterApplicationTests.java`

**Observation:**
Test coverage is below 5%. Both sides contain a single placeholder/smoke test. No tests exist for the core logic: Claude response parsing, test execution, DTO mapping, or any React component.

**Remediation:**
Establish a minimum coverage gate of 60% on service-layer classes. Priority targets:
- `ClaudeService.parseTestCases()` — mock WebClient, assert DTO mapping
- `TestResultMapper.toDto()` — pure unit test, zero infrastructure needed
- `TestExecutorService.executeSingleTest()` — WireMock for HTTP stubbing
- Frontend: React Testing Library tests for `UploadApi.tsx` and `Results.tsx`

---

#### AUDIT-M02 — No Structured Logging or Observability

**Severity:** Medium
**Files:** `application.yaml`, all service classes

**Observation:**
Logs are emitted as plain text. No request correlation IDs, no structured fields, no metrics endpoint, no distributed tracing. In a team environment with a log aggregation stack, plain-text logs are not searchable or alertable.

**Remediation:**
- Add `logstash-logback-encoder` for JSON log output.
- Add `spring-boot-starter-actuator` + Micrometer Prometheus registry; expose `/actuator/metrics`.
- Add MDC request ID filter (generates a UUID per request; attaches to all log lines).
- Long-term: Spring Cloud Sleuth + Zipkin or OpenTelemetry for distributed tracing.

---

#### AUDIT-M03 — No Retry or Circuit Breaker on Claude API

**Severity:** Medium
**Files:** `ClaudeService.java:227–236`

**Observation:**
The Claude API call uses `.block()` with no retry policy. A transient 429 (rate limit) or 503 from Anthropic fails the entire test generation request with no recovery.

**Remediation:**
Add `resilience4j-spring-boot3` dependency. Annotate `callClaude()` with:
```java
@Retry(name = "claude", fallbackMethod = "callClaudeFallback")
@CircuitBreaker(name = "claude")
```
Configure 3 retries with exponential backoff (1s, 2s, 4s) in `application.yaml`.

---

#### AUDIT-M04 — System Prompt Embedded in Source Code

**Severity:** Medium
**Files:** `ClaudeService.java:41–53`

**Observation:**
The 13-line system prompt is a `private static final String` constant. Iterating on prompt quality requires a code change, a build, and a redeploy. Impossible to A/B test without code branching.

**Remediation:**
Move to a file in `src/main/resources/prompts/test-generation.txt`. Load with `@Value("classpath:prompts/test-generation.txt")`. For team iteration without redeploy, store in the database with a version column and a simple admin endpoint.

---

#### AUDIT-M05 — Kubernetes Single Replica and Missing Resource Constraints

**Severity:** Medium
**Files:** `k8s/03-backend.yaml`, `k8s/04-frontend.yaml`

**Observation:**
Both deployments specify `replicas: 1`. No `resources.requests`/`resources.limits` are set, so pods can starve or consume unbounded CPU/memory. No `HorizontalPodAutoscaler` or `PodDisruptionBudget` exists.

**Remediation:**
```yaml
replicas: 2
resources:
  requests:
    cpu: "250m"
    memory: "512Mi"
  limits:
    cpu: "1000m"
    memory: "1Gi"
```
Add a `HorizontalPodAutoscaler` targeting 70% CPU utilisation with `minReplicas: 2`, `maxReplicas: 6`.

---

#### AUDIT-M06 — JSONB Columns Typed as String in Entities

**Severity:** Medium
**Files:** `TestCase.java`, `TestResult.java`

**Observation:**
Fields like `headers`, `payload`, `actualResponse`, and `expectedSchema` are declared as `private String` in the JPA entity but are stored as PostgreSQL `jsonb`. The serialisation/deserialisation is handled implicitly by Jackson through string conversion. This is fragile — any field read back as a raw string is not queryable as JSONB and will break if a non-JSON string is stored.

**Remediation:**
Implement a JPA `@Converter` for `Map<String, Object>` ↔ `jsonb`, or use Hibernate Types (`io.hypersistence:hypersistence-utils`) to declare:
```java
@Type(JsonType.class)
@Column(columnDefinition = "jsonb")
private Map<String, Object> payload;
```

---

#### AUDIT-M07 — `expected_schema` Stored but Never Validated

**Severity:** Medium
**Files:** `TestCase.java`, `TestExecutorService.java`

**Observation:**
`expectedSchema` is generated by Claude, persisted in the database, and then completely ignored during execution. Test validation only checks `actualStatus == expectedStatus`.

**Remediation:**
Integrate `everit-org/json-schema` or `networknt/json-schema-validator`. After receiving the response body, validate it against `expectedSchema` if present. Populate a new `schemaValid` boolean on `TestResult`. Surface schema violations in the results table and Excel export.

---

### 3.4 Low-Priority Observations

| ID | Observation | File |
|----|-------------|------|
| AUDIT-L01 | `data/mock.ts` is imported nowhere — dead file | `Frontend/src/data/mock.ts` |
| AUDIT-L02 | K8s image tags pinned to `latest` — non-deterministic deploys | `k8s/03-backend.yaml:17` |
| AUDIT-L03 | Docker Compose has no restart policy on `db` service | `docker-compose.yml` |
| AUDIT-L04 | No DB composite index on `(run_id, created_at DESC)` — slow at scale | Schema / migrations |
| AUDIT-L05 | `User` entity in `entity/` package while other models are in `model/` — inconsistent | `entity/User.java` |
| AUDIT-L06 | `build-and-push.ps1` hardcodes Docker Hub username — not parameterised | `build-and-push.ps1` |
| AUDIT-L07 | `vite.config.ts` sets `server.port: 8080` — clashes with backend default | `Frontend/vite.config.ts` |

---

## 4. Database Schema Analysis

### Current Schema

```sql
test_runs
  id              UUID        PRIMARY KEY
  spec_filename   TEXT
  instructions    TEXT
  environment     TEXT
  status          TEXT        -- SPEC_UPLOADED | TESTS_GENERATED | RUNNING | COMPLETED
  created_at      TIMESTAMP
  completed_at    TIMESTAMP   (nullable)

test_cases
  id              UUID        PRIMARY KEY
  run_id          UUID        -- no FK constraint
  name            TEXT
  endpoint        TEXT
  method          VARCHAR(10)
  headers         JSONB
  payload         JSONB
  expected_status INTEGER
  expected_schema JSONB
  test_type       TEXT
  description     TEXT
  chain_from      UUID        (nullable)
  created_at      TIMESTAMP

test_results
  id              UUID        PRIMARY KEY
  test_case_id    UUID        -- no FK constraint
  run_id          UUID        -- no FK constraint
  actual_status   INTEGER
  actual_response JSONB
  response_time_ms BIGINT
  passed          BOOLEAN
  error_message   TEXT
  executed_at     TIMESTAMP
```

### Required Additions

```sql
-- Enforce referential integrity
ALTER TABLE test_cases
  ADD CONSTRAINT fk_test_cases_run_id
  FOREIGN KEY (run_id) REFERENCES test_runs(id) ON DELETE CASCADE;

ALTER TABLE test_results
  ADD CONSTRAINT fk_test_results_run_id
  FOREIGN KEY (run_id) REFERENCES test_runs(id) ON DELETE CASCADE;

ALTER TABLE test_results
  ADD CONSTRAINT fk_test_results_test_case_id
  FOREIGN KEY (test_case_id) REFERENCES test_cases(id) ON DELETE CASCADE;

-- Query performance
CREATE INDEX idx_test_cases_run_id      ON test_cases(run_id);
CREATE INDEX idx_test_results_run_id    ON test_results(run_id, executed_at DESC);
CREATE INDEX idx_test_results_test_case ON test_results(test_case_id);

-- Multi-tenancy (Phase 4)
ALTER TABLE test_runs ADD COLUMN user_id UUID REFERENCES users(id);
CREATE INDEX idx_test_runs_user_id ON test_runs(user_id);
```

---

## 5. Security Summary

| Vulnerability | CVSS | Status |
|--------------|------|--------|
| No authentication — all endpoints public | 9.1 Critical | Open |
| CORS wildcard | 6.5 Medium | Open |
| No rate limiting on `/api/tests/generate` | 7.5 High | Open |
| No input validation on DTOs | 6.5 Medium | Open |
| Secrets in `.env` file (plain text) | 5.3 Medium | Open |
| Payload logged at INFO (data exposure) | 4.3 Medium | Open |
| No audit log (who ran what, when) | 4.0 Medium | Open |

---

## 6. Performance Estimates

| Improvement | Complexity | Expected Gain |
|-------------|-----------|---------------|
| Parallel test execution | Medium | ~10× faster test runs |
| Pagination on results endpoint | Low | ~100× memory reduction at scale |
| DB indexes on run_id columns | Low | ~5× faster result queries |
| Spring Cache on parsed specs | Low | ~2× faster test generation |
| Async Claude API call | Medium | ~3× backend throughput |

---

## 7. Priority Roadmap

### Phase 1 — Security & Stability (Weeks 1–2)
- [ ] Implement JWT authentication end-to-end (backend + frontend interceptor)
- [ ] Enable Flyway, write `V1__init.sql`
- [ ] Add `GlobalExceptionHandler` (`@ControllerAdvice`)
- [ ] Restrict CORS to configured origin whitelist
- [ ] Enable TypeScript `strict` mode; fix type errors
- [ ] Add `@Valid` to all controller request bodies

### Phase 2 — Code Quality (Weeks 2–3)
- [ ] Create `TestOrchestrationService` (extract logic from `TestController`)
- [ ] Create `TestResultMapper` (eliminate duplicated mapping)
- [ ] Split `api.ts` into `client.ts` + domain-specific API modules
- [ ] Externalize `SYSTEM_PROMPT` to `resources/prompts/`
- [ ] Externalize Claude pricing to `application.yaml` via `@ConfigurationProperties`
- [ ] Move payload logging from INFO to DEBUG in `ClaudeService`

### Phase 3 — Observability & Reliability (Weeks 3–4)
- [ ] Add structured JSON logging (Logback + Logstash encoder)
- [ ] Add Micrometer + Prometheus metrics (`/actuator/metrics`)
- [ ] Add Resilience4j retry + circuit breaker on Claude API
- [ ] Add Axios `timeout: 30_000` and auth interceptor to frontend
- [ ] Write first 20 unit tests (service layer — no infrastructure needed)

### Phase 4 — Performance & Scale (Weeks 4–6)
- [ ] Paginate `GET /api/tests/results/{runId}`
- [ ] Parallelize `TestExecutorService` for independent test cases
- [ ] Add DB indexes (see schema section)
- [ ] Add `HorizontalPodAutoscaler` and resource limits to K8s manifests
- [ ] Increase K8s replicas to 2 minimum

### Phase 5 — Enterprise Readiness (Ongoing)
- [ ] Multi-tenancy (`user_id` on `test_runs`, row-level isolation)
- [ ] JSON Schema validation on test results
- [ ] Cost tracking dashboard (token usage by user, model, date)
- [ ] Springdoc OpenAPI for auto-generated API docs at `/swagger-ui.html`
- [ ] E2E tests with Playwright or Cypress

---

## 8. Files Audited

| File | Lines | Audit Notes |
|------|-------|-------------|
| `ClaudeService.java` | 328 | Hardcoded pricing, INFO-level payload log, no retry |
| `TestController.java` | 263 | Duplicated mapping, repos in controller, no exception handling |
| `TestExecutorService.java` | 200+ | Sequential, brittle ID regex |
| `ExcelExportService.java` | ~100 | Clean; no issues |
| `SpecParserService.java` | ~80 | Clean; no issues |
| `WebClientConfig.java` | 45 | Inconsistent timeout vs. executor |
| `application.yaml` | 33 | Flyway disabled, dummy API key placeholder |
| `TestCase.java` / `TestResult.java` | 72 / 56 | JSONB typed as String, no FK constraints |
| `Frontend/src/lib/api.ts` | 247 | No timeout, no auth interceptor, monolithic |
| `Frontend/src/store/appStore.ts` | 132 | Mock session, AuthType not a union type |
| `Frontend/tsconfig.json` | 16 | Strict mode off |
| `Frontend/src/App.tsx` | 57 | Router and providers — clean |
| `docker-compose.yml` | 63 | No restart policy on db |
| `k8s/03-backend.yaml` | 63 | 1 replica, no resources, no liveness probe |
| `k8s/04-frontend.yaml` | ~50 | Same K8s gaps as backend |

---

*Audit completed: 2026-04-27. Next review recommended after Phase 1 & 2 completion.*

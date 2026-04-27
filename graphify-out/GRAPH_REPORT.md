# Graph Report - .  (2026-04-26)

## Corpus Check
- Corpus is ~31,119 words - fits in a single context window. You may not need a graph.

## Summary
- 417 nodes · 362 edges · 52 communities detected
- Extraction: 83% EXTRACTED · 17% INFERRED · 0% AMBIGUOUS · INFERRED: 60 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Frontend App Shell|Frontend App Shell]]
- [[_COMMUNITY_API Response DTOs|API Response DTOs]]
- [[_COMMUNITY_API Client Layer|API Client Layer]]
- [[_COMMUNITY_Test Execution Service|Test Execution Service]]
- [[_COMMUNITY_Toast Notification System|Toast Notification System]]
- [[_COMMUNITY_Auth Controller|Auth Controller]]
- [[_COMMUNITY_Frontend Pages and Utils|Frontend Pages and Utils]]
- [[_COMMUNITY_Claude AI Service|Claude AI Service]]
- [[_COMMUNITY_Pagination Component|Pagination Component]]
- [[_COMMUNITY_Frontend Routing|Frontend Routing]]
- [[_COMMUNITY_Brand Assets and Favicon|Brand Assets and Favicon]]
- [[_COMMUNITY_HTTP Client Config|HTTP Client Config]]
- [[_COMMUNITY_Excel Export Service|Excel Export Service]]
- [[_COMMUNITY_Drawer UI Component|Drawer UI Component]]
- [[_COMMUNITY_Sidebar Navigation|Sidebar Navigation]]
- [[_COMMUNITY_Spring Boot Entry|Spring Boot Entry]]
- [[_COMMUNITY_TestCase Domain Model|TestCase Domain Model]]
- [[_COMMUNITY_TestResult Domain Model|TestResult Domain Model]]
- [[_COMMUNITY_TestRun Domain Model|TestRun Domain Model]]
- [[_COMMUNITY_TestCase Repository|TestCase Repository]]
- [[_COMMUNITY_Component 20|Component 20]]
- [[_COMMUNITY_Component 21|Component 21]]
- [[_COMMUNITY_Component 22|Component 22]]
- [[_COMMUNITY_Component 23|Component 23]]
- [[_COMMUNITY_Component 24|Component 24]]
- [[_COMMUNITY_Component 25|Component 25]]
- [[_COMMUNITY_Component 26|Component 26]]
- [[_COMMUNITY_Component 27|Component 27]]
- [[_COMMUNITY_Component 28|Component 28]]
- [[_COMMUNITY_Component 29|Component 29]]
- [[_COMMUNITY_Component 30|Component 30]]
- [[_COMMUNITY_Component 31|Component 31]]
- [[_COMMUNITY_Component 32|Component 32]]
- [[_COMMUNITY_Component 33|Component 33]]
- [[_COMMUNITY_Component 34|Component 34]]
- [[_COMMUNITY_Component 35|Component 35]]
- [[_COMMUNITY_Component 36|Component 36]]
- [[_COMMUNITY_Component 37|Component 37]]
- [[_COMMUNITY_Component 38|Component 38]]
- [[_COMMUNITY_Component 39|Component 39]]
- [[_COMMUNITY_Component 40|Component 40]]
- [[_COMMUNITY_Component 41|Component 41]]
- [[_COMMUNITY_Component 42|Component 42]]
- [[_COMMUNITY_Component 43|Component 43]]
- [[_COMMUNITY_Component 44|Component 44]]
- [[_COMMUNITY_Component 45|Component 45]]
- [[_COMMUNITY_Component 46|Component 46]]
- [[_COMMUNITY_Component 47|Component 47]]
- [[_COMMUNITY_Component 48|Component 48]]
- [[_COMMUNITY_Component 49|Component 49]]
- [[_COMMUNITY_Component 50|Component 50]]
- [[_COMMUNITY_Component 163|Component 163]]

## God Nodes (most connected - your core abstractions)
1. `TestExecutorService` - 12 edges
2. `TestController` - 8 edges
3. `extractErrorMessage()` - 8 edges
4. `TestBuddy Project` - 8 edges
5. `React 18 + TypeScript 5.8 Frontend` - 8 edges
6. `Spring Boot 3.2 Backend` - 7 edges
7. `ClaudeService` - 6 edges
8. `SpecParserService` - 5 edges
9. `runTests()` - 5 edges
10. `handleGenerate()` - 5 edges

## Surprising Connections (you probably didn't know these)
- `Spring Reactive Web` --semantically_similar_to--> `Spring WebFlux`  [INFERRED] [semantically similar]
  Backend/ai-api-tester/HELP.md → README.md
- `runAll()` --calls--> `runTests()`  [INFERRED]
  C:\Users\yashb\Pictures\Projects\TestBuddy\Frontend\src\pages\TestCases.tsx → C:\Users\yashb\Pictures\Projects\TestBuddy\Frontend\src\lib\api.ts
- `runSingle()` --calls--> `runTests()`  [INFERRED]
  C:\Users\yashb\Pictures\Projects\TestBuddy\Frontend\src\pages\TestCases.tsx → C:\Users\yashb\Pictures\Projects\TestBuddy\Frontend\src\lib\api.ts
- `handleExport()` --calls--> `exportReport()`  [INFERRED]
  C:\Users\yashb\Pictures\Projects\TestBuddy\Frontend\src\pages\Results.tsx → C:\Users\yashb\Pictures\Projects\TestBuddy\Frontend\src\lib\api.ts
- `Toaster()` --calls--> `useToast()`  [INFERRED]
  C:\Users\yashb\Pictures\Projects\TestBuddy\Frontend\src\components\ui\toaster.tsx → C:\Users\yashb\Pictures\Projects\TestBuddy\Frontend\src\hooks\use-toast.ts

## Hyperedges (group relationships)
- **Backend Service Layer: ClaudeService, GeminiService, TestExecutorService, ExcelExportService** — readme_claudeservice, readme_geminiservice, readme_testexecutorservice, readme_excelexportservice [EXTRACTED 0.95]
- **Database Schema: test_runs, test_cases, test_results form a hierarchical test session model** — readme_db_testruns, readme_db_testcases, readme_db_testresults [EXTRACTED 1.00]
- **Frontend UI Stack: React 18, Shadcn/ui, Recharts, Zustand form the complete frontend architecture** — readme_react18, readme_shadcnui, readme_recharts, readme_zustand [EXTRACTED 0.92]

## Communities

### Community 0 - "Frontend App Shell"
Cohesion: 0.08
Nodes (9): ApiResponse, ExcelExportService, InsightController, handleExplainFailure(), handleExport(), SpecController, SpecParserService, TestController (+1 more)

### Community 1 - "API Response DTOs"
Cohesion: 0.08
Nodes (32): Backend HELP.md (Spring Boot Setup Notes), Apache Maven Build Tool, Package Name Fix: com.testai.ai_api_tester, Spring Data JPA, Spring Reactive Web, AI Insights Page, Apache POI, Claude Haiku 4.5 (+24 more)

### Community 2 - "API Client Layer"
Cohesion: 0.32
Nodes (10): explainFailure(), exportReport(), extractErrorMessage(), generateTests(), getResults(), runTests(), uploadSpec(), handleDrop() (+2 more)

### Community 3 - "Test Execution Service"
Cohesion: 0.27
Nodes (1): TestExecutorService

### Community 4 - "Toast Notification System"
Cohesion: 0.18
Nodes (12): Frontend Entry Point (index.html), Lovable.dev Platform, Frontend Main Entry (main.tsx), Frontend README (Lovable Project), Axios HTTP Client, React 18 + TypeScript 5.8 Frontend, React Hook Form + Zod, Recharts (+4 more)

### Community 5 - "Auth Controller"
Cohesion: 0.35
Nodes (7): Toaster(), addToRemoveQueue(), dispatch(), genId(), reducer(), toast(), useToast()

### Community 6 - "Frontend Pages and Utils"
Cohesion: 0.22
Nodes (2): AuthController, UserRepository

### Community 7 - "Claude AI Service"
Cohesion: 0.33
Nodes (5): Profile(), accountDisplayName(), cn(), displayNameFromEmail(), initialsFromEmail()

### Community 8 - "Pagination Component"
Cohesion: 0.48
Nodes (1): ClaudeService

### Community 9 - "Frontend Routing"
Cohesion: 0.48
Nodes (5): Pagination(), PaginationEllipsis(), PaginationLink(), PaginationNext(), PaginationPrevious()

### Community 10 - "Brand Assets and Favicon"
Cohesion: 0.29
Nodes (1): NotFound()

### Community 11 - "HTTP Client Config"
Cohesion: 0.48
Nodes (5): getResponseCode(), getResponseTime(), mapStatus(), runAll(), runSingle()

### Community 12 - "Excel Export Service"
Cohesion: 0.47
Nodes (6): Blue-to-Indigo Linear Gradient, Rounded Square Background Shape, Sparkles Icon (Lucide), TestBuddy Favicon SVG, Lucide Icon Library, TestBuddy Project

### Community 13 - "Drawer UI Component"
Cohesion: 0.4
Nodes (1): WebClientConfig

### Community 14 - "Sidebar Navigation"
Cohesion: 0.6
Nodes (3): Drawer(), DrawerFooter(), DrawerHeader()

### Community 15 - "Spring Boot Entry"
Cohesion: 0.6
Nodes (3): cn(), handleKeyDown(), useSidebar()

### Community 16 - "TestCase Domain Model"
Cohesion: 0.5
Nodes (1): AiApiTesterApplication

### Community 17 - "TestResult Domain Model"
Cohesion: 0.5
Nodes (1): TestCase

### Community 18 - "TestRun Domain Model"
Cohesion: 0.5
Nodes (1): TestResult

### Community 19 - "TestCase Repository"
Cohesion: 0.5
Nodes (1): TestRun

### Community 20 - "Component 20"
Cohesion: 0.5
Nodes (1): TestCaseRepository

### Community 21 - "Component 21"
Cohesion: 0.5
Nodes (1): AiApiTesterApplicationTests

### Community 22 - "Component 22"
Cohesion: 0.67
Nodes (2): getInitialTheme(), ThemeProvider()

### Community 23 - "Component 23"
Cohesion: 0.67
Nodes (2): BreadcrumbEllipsis(), BreadcrumbSeparator()

### Community 24 - "Component 24"
Cohesion: 0.67
Nodes (2): ApiKeys(), randomKeySegment()

### Community 25 - "Component 25"
Cohesion: 0.67
Nodes (2): markAllRead(), toggleRead()

### Community 26 - "Component 26"
Cohesion: 0.67
Nodes (1): GenerateRequest

### Community 27 - "Component 27"
Cohesion: 0.67
Nodes (1): InsightRequest

### Community 28 - "Component 28"
Cohesion: 0.67
Nodes (1): InsightResponse

### Community 29 - "Component 29"
Cohesion: 0.67
Nodes (1): ResultsSummaryDto

### Community 30 - "Component 30"
Cohesion: 0.67
Nodes (1): RunTestsRequest

### Community 31 - "Component 31"
Cohesion: 0.67
Nodes (1): TestCaseDto

### Community 32 - "Component 32"
Cohesion: 0.67
Nodes (1): TestResultDto

### Community 33 - "Component 33"
Cohesion: 0.67
Nodes (1): User

### Community 34 - "Component 34"
Cohesion: 0.67
Nodes (1): TestRunRepository

### Community 35 - "Component 35"
Cohesion: 0.67
Nodes (1): handleSignOut()

### Community 36 - "Component 36"
Cohesion: 0.67
Nodes (1): AppLayout()

### Community 37 - "Component 37"
Cohesion: 0.67
Nodes (1): ProtectedOutlet()

### Community 38 - "Component 38"
Cohesion: 0.67
Nodes (1): MethodBadge()

### Community 39 - "Component 39"
Cohesion: 0.67
Nodes (1): StatusBadge()

### Community 40 - "Component 40"
Cohesion: 0.67
Nodes (1): Calendar()

### Community 41 - "Component 41"
Cohesion: 0.67
Nodes (1): useCarousel()

### Community 42 - "Component 42"
Cohesion: 0.67
Nodes (1): useChart()

### Community 43 - "Component 43"
Cohesion: 0.67
Nodes (1): useFormField()

### Community 44 - "Component 44"
Cohesion: 0.67
Nodes (1): ResizablePanelGroup()

### Community 45 - "Component 45"
Cohesion: 0.67
Nodes (1): Toaster()

### Community 46 - "Component 46"
Cohesion: 0.67
Nodes (1): useIsMobile()

### Community 47 - "Component 47"
Cohesion: 0.67
Nodes (1): SignIn()

### Community 48 - "Component 48"
Cohesion: 0.67
Nodes (1): SignUp()

### Community 49 - "Component 49"
Cohesion: 1.0
Nodes (3): DB Table: test_cases, DB Table: test_results, DB Table: test_runs

### Community 50 - "Component 50"
Cohesion: 1.0
Nodes (2): Placeholder SVG Logo, Text Wordmark Design

### Community 163 - "Component 163"
Cohesion: 1.0
Nodes (1): robots.txt (Crawler Policy)

## Knowledge Gaps
- **18 isolated node(s):** `API Testing Problem Domain`, `Vite 5.4`, `Shadcn/ui (Radix UI + Tailwind CSS)`, `React Hook Form + Zod`, `Docker + Docker Compose` (+13 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Test Execution Service`** (13 nodes): `TestExecutorService.java`, `TestExecutorService.java`, `TestExecutorService`, `.buildFailedResult()`, `.executeSingleTest()`, `.executeTests()`, `.extractIdFromResponse()`, `.isBodyMethod()`, `.normalizeUrl()`, `.parseResponseBody()`, `.parseUuidSafe()`, `.substituteId()`, `.toJsonSafe()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Frontend Pages and Utils`** (9 nodes): `AuthController`, `.signin()`, `.signup()`, `AuthController.java`, `UserRepository.java`, `AuthController.java`, `UserRepository.java`, `UserRepository`, `.findByEmail()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Pagination Component`** (7 nodes): `ClaudeService.java`, `ClaudeService.java`, `ClaudeService`, `.callClaude()`, `.explainFailure()`, `.generateTestCases()`, `.stripMarkdownFences()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Brand Assets and Favicon`** (7 nodes): `App.tsx`, `main.tsx`, `NotFound.tsx`, `App.tsx`, `main.tsx`, `NotFound.tsx`, `NotFound()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Drawer UI Component`** (5 nodes): `WebClientConfig.java`, `WebClientConfig.java`, `WebClientConfig`, `.objectMapper()`, `.webClient()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TestCase Domain Model`** (4 nodes): `AiApiTesterApplication`, `.main()`, `AiApiTesterApplication.java`, `AiApiTesterApplication.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TestResult Domain Model`** (4 nodes): `TestCase.java`, `TestCase.java`, `TestCase`, `.prePersist()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TestRun Domain Model`** (4 nodes): `TestResult.java`, `TestResult.java`, `TestResult`, `.prePersist()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TestCase Repository`** (4 nodes): `TestRun.java`, `TestRun.java`, `TestRun`, `.prePersist()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 20`** (4 nodes): `TestCaseRepository.java`, `TestCaseRepository.java`, `TestCaseRepository`, `.findByRunId()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 21`** (4 nodes): `AiApiTesterApplicationTests`, `.contextLoads()`, `AiApiTesterApplicationTests.java`, `AiApiTesterApplicationTests.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 22`** (4 nodes): `ThemeProvider.tsx`, `ThemeProvider.tsx`, `getInitialTheme()`, `ThemeProvider()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 23`** (4 nodes): `BreadcrumbEllipsis()`, `BreadcrumbSeparator()`, `breadcrumb.tsx`, `breadcrumb.tsx`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 24`** (4 nodes): `ApiKeys()`, `randomKeySegment()`, `ApiKeys.tsx`, `ApiKeys.tsx`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 25`** (4 nodes): `Notifications.tsx`, `Notifications.tsx`, `markAllRead()`, `toggleRead()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 26`** (3 nodes): `GenerateRequest.java`, `GenerateRequest.java`, `GenerateRequest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 27`** (3 nodes): `InsightRequest.java`, `InsightRequest.java`, `InsightRequest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 28`** (3 nodes): `InsightResponse.java`, `InsightResponse.java`, `InsightResponse`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 29`** (3 nodes): `ResultsSummaryDto.java`, `ResultsSummaryDto.java`, `ResultsSummaryDto`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 30`** (3 nodes): `RunTestsRequest.java`, `RunTestsRequest.java`, `RunTestsRequest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 31`** (3 nodes): `TestCaseDto.java`, `TestCaseDto.java`, `TestCaseDto`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 32`** (3 nodes): `TestResultDto.java`, `TestResultDto.java`, `TestResultDto`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 33`** (3 nodes): `User.java`, `User.java`, `User`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 34`** (3 nodes): `TestRunRepository.java`, `TestRunRepository.java`, `TestRunRepository`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 35`** (3 nodes): `handleSignOut()`, `AppHeader.tsx`, `AppHeader.tsx`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 36`** (3 nodes): `AppLayout()`, `AppLayout.tsx`, `AppLayout.tsx`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 37`** (3 nodes): `ProtectedOutlet.tsx`, `ProtectedOutlet.tsx`, `ProtectedOutlet()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 38`** (3 nodes): `MethodBadge.tsx`, `MethodBadge.tsx`, `MethodBadge()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 39`** (3 nodes): `StatusBadge.tsx`, `StatusBadge.tsx`, `StatusBadge()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 40`** (3 nodes): `calendar.tsx`, `Calendar()`, `calendar.tsx`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 41`** (3 nodes): `carousel.tsx`, `useCarousel()`, `carousel.tsx`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 42`** (3 nodes): `chart.tsx`, `useChart()`, `chart.tsx`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 43`** (3 nodes): `form.tsx`, `useFormField()`, `form.tsx`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 44`** (3 nodes): `resizable.tsx`, `resizable.tsx`, `ResizablePanelGroup()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 45`** (3 nodes): `sonner.tsx`, `sonner.tsx`, `Toaster()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 46`** (3 nodes): `use-mobile.tsx`, `use-mobile.tsx`, `useIsMobile()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 47`** (3 nodes): `SignIn.tsx`, `SignIn.tsx`, `SignIn()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 48`** (3 nodes): `SignUp.tsx`, `SignUp.tsx`, `SignUp()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 50`** (2 nodes): `Placeholder SVG Logo`, `Text Wordmark Design`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Component 163`** (1 nodes): `robots.txt (Crawler Policy)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `TestBuddy Project` connect `API Response DTOs` to `Toast Notification System`?**
  _High betweenness centrality (0.006) - this node is a cross-community bridge._
- **Why does `handleGenerate()` connect `API Client Layer` to `Frontend App Shell`?**
  _High betweenness centrality (0.006) - this node is a cross-community bridge._
- **What connects `API Testing Problem Domain`, `Vite 5.4`, `Shadcn/ui (Radix UI + Tailwind CSS)` to the rest of the system?**
  _18 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Frontend App Shell` be split into smaller, more focused modules?**
  _Cohesion score 0.08 - nodes in this community are weakly interconnected._
- **Should `API Response DTOs` be split into smaller, more focused modules?**
  _Cohesion score 0.08 - nodes in this community are weakly interconnected._
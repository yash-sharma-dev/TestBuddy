
# TestBuddy — AI-Powered API Testing Platform

A polished, developer-focused SaaS web app (React + Tailwind, fully responsive). Mock data throughout — no backend yet.

## Design System
- **Palette** (HSL tokens in `index.css`): Primary deep blue `#2563EB`, secondary indigo `#4F46E5`, accent cyan `#06B6D4`, success green `#10B981`, destructive red `#EF4444`, bg `#F9FAFB`, card white, text `#111827`.
- **Typography:** Inter (Google Fonts), tight tracking on headings.
- **Surfaces:** 12–16px rounded corners, soft layered shadows, subtle blue→indigo gradients on hero/CTA elements.
- **Motion:** fade-in, scale-in, hover-lift, smooth sidebar collapse, animated badges and chart entrance.

## Layout (shared shell)
- **Collapsible left sidebar** (shadcn Sidebar, `collapsible="icon"`) with: Dashboard, Upload API, Test Cases, Results, AI Insights.
- **Top header:** TestBuddy logo (gradient mark + wordmark), global sidebar trigger, search, notifications bell with dot, user avatar dropdown.
- **Main area:** generous padding, max-width container, consistent card spacing.

## Pages

### 1. Dashboard (`/`)
- Gradient welcome banner: "AI API Test Automation with TestBuddy" + subtitle + primary CTA.
- 4 summary stat cards: Total APIs, Total Tests, Passed (green), Failed (red) — each with icon, big number, trend delta.
- Recent Activity feed: latest test runs with status badge, endpoint, timestamp, duration.
- Mini line chart showing test runs over the last 7 days.

### 2. Upload API (`/upload`)
- Centered card with large dashed drag-and-drop zone (cloud-upload icon, hover/active states, accepts `.json`/`.yaml`).
- Base URL input with prefix adornment.
- Helpful hints list (supported formats, examples).
- Primary "Generate Test Cases" button with loading state → toast on success.

### 3. Test Cases (`/test-cases`)
- Filter tabs: All / Passed / Failed / Not Run + search input.
- Data table: Test Name, Endpoint, Method (color-coded badges: GET blue, POST green, PUT amber, DELETE red), Payload (View → modal with formatted JSON), Expected Status, Actions (run single).
- Sticky toolbar with "Run All Tests" primary button (animated progress when running).

### 4. Results Dashboard (`/results`)
- Top row: 2 charts (recharts) — Pie chart Pass vs Fail, Bar chart avg response time per endpoint.
- Results table: Test Name, Status badge (✅ Pass / ❌ Fail), Response Code, Response Time, Error Message (truncated, expandable).
- Export button (mock).

### 5. AI Insights (`/insights`)
- Two-column on desktop: left = list of failed tests (selectable), right = chat-style AI explanation card with avatar, formatted markdown-style sections (Root Cause, Suggested Fix, Code Snippet).
- "Ask follow-up" input at bottom of chat panel (mock response).
- Highlight key issues with colored callouts.

## Components to build
- `AppSidebar`, `AppHeader`, `AppLayout`
- `StatCard`, `MethodBadge`, `StatusBadge`, `ActivityItem`
- `UploadDropzone`, `PayloadDialog`
- `ResultsPieChart`, `PerformanceBarChart`, `TrendLineChart`
- `InsightChatCard`

## Routing
Add routes in `App.tsx` for `/`, `/upload`, `/test-cases`, `/results`, `/insights`, all wrapped in `AppLayout`.

## Responsiveness
- Sidebar collapses to icon rail on tablet, off-canvas drawer on mobile.
- Stat cards: 4 cols → 2 cols → 1 col.
- Tables become horizontally scrollable with sticky first column on mobile.

Mock/fixture data lives in `src/data/mock.ts` so every page feels alive immediately.

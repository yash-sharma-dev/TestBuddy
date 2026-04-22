import axios, { AxiosError } from "axios";

// ── Types ────────────────────────────────────────────────────────────────────

export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH";

export interface ApiResponse<T> {
  success: boolean;
  data: T;
}

/** Shape returned by POST /api/spec/upload */
export interface ParsedSpec {
  runId: string;
  title: string;
  endpointCount: number;
  endpoints: string[];
  [key: string]: unknown; // additional fields from the backend
}

/** Shape returned by POST /api/tests/generate and sent back on run */
export interface TestCaseDto {
  id: string;
  name: string;
  endpoint: string;
  method: HttpMethod;
  headers: Record<string, string> | null;
  payload: Record<string, unknown> | null;
  expectedStatus: number;
  expectedSchema: string | null;
  testType: string;
  chainFrom: string | null;
  description: string;
}

/** Shape returned inside POST /api/tests/run results array */
export interface TestResult {
  testCaseId: string;
  name: string;
  endpoint: string;
  method: HttpMethod;
  payload: Record<string, unknown> | null;
  expectedStatus: number;
  actualStatus: number;
  actualResponse: string | null;
  responseTimeMs: number;
  passed: boolean;
  errorMessage: string | null;
}

/** Summary block inside GET /api/tests/results/{runId} */
export interface ResultSummary {
  total: number;
  passed: number;
  failed: number;
  avgResponseTime: number;
}

/** Params for POST /api/tests/generate */
export interface GenerateTestsParams {
  runId: string;
  instructions: string;
  targetBaseUrl: string;
  environment: string;
  authType: string;
  authValue: string;
}

/** Params for POST /api/tests/run */
export interface RunTestsParams {
  runId: string;
  testCases: TestCaseDto[];
  targetBaseUrl: string;
  authType: string;
  authValue: string;
}

/** Request body for POST /api/insights/explain */
export interface InsightExplainRequest {
  testCaseName: string;
  endpoint: string;
  method: string;
  payload: Record<string, unknown> | null;
  expectedStatus: number;
  actualStatus: number;
  errorMessage: string | null;
}

/** Response from POST /api/insights/explain */
export interface InsightExplainResponse {
  technical: string;
  human: string;
  suggestion: string;
}

// ── Axios instance ───────────────────────────────────────────────────────────

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
});

/** Extract a human-readable error message from any Axios error. */
function extractErrorMessage(error: unknown): string {
  if (error instanceof AxiosError) {
    const data = error.response?.data as Record<string, unknown> | undefined;
    if (data?.message && typeof data.message === "string") return data.message;
    if (data?.error && typeof data.error === "string") return data.error;
    if (error.response) return `Server responded with ${error.response.status}`;
    if (error.request) return "No response from server — is the backend running?";
    return error.message;
  }
  if (error instanceof Error) return error.message;
  return "An unexpected error occurred";
}

// ── API Functions ────────────────────────────────────────────────────────────

/**
 * Upload an OpenAPI spec file.
 * POST /api/spec/upload (multipart/form-data)
 */
export async function uploadSpec(
  file: File,
  environment: string,
): Promise<ApiResponse<ParsedSpec>> {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("environment", environment);

  try {
    const { data } = await api.post<ApiResponse<ParsedSpec>>(
      "/api/spec/upload",
      formData,
      { headers: { "Content-Type": "multipart/form-data" } },
    );
    return data;
  } catch (err) {
    throw new Error(extractErrorMessage(err));
  }
}

/**
 * Generate test cases from a parsed spec.
 * POST /api/tests/generate?spec=<raw content>
 */
export async function generateTests(
  specContent: string,
  params: GenerateTestsParams,
): Promise<TestCaseDto[]> {
  try {
    const { data } = await api.post<ApiResponse<TestCaseDto[]>>(
      "/api/tests/generate",
      {
        runId: params.runId,
        instructions: params.instructions,
        spec: specContent,
        targetBaseUrl: params.targetBaseUrl,
        environment: params.environment,
        authType: params.authType,
        authValue: params.authValue,
      },
    );
    
    if (!data.success) {
      throw new Error(data.error || "Failed to generate tests");
    }
    
    return data.data || [];
  } catch (err) {
    throw new Error(extractErrorMessage(err));
  }
}

/**
 * Execute test cases against the target API.
 * POST /api/tests/run
 */
export async function runTests(
  params: RunTestsParams,
): Promise<ApiResponse<{ runId: string; results: TestResult[] }>> {
  try {
    const { data } = await api.post<
      ApiResponse<{ runId: string; results: TestResult[] }>
    >("/api/tests/run", params);
    return data;
  } catch (err) {
    throw new Error(extractErrorMessage(err));
  }
}

/**
 * Fetch results for a completed run.
 * GET /api/tests/results/{runId}
 */
export async function getResults(
  runId: string,
): Promise<
  ApiResponse<{ runId: string; results: TestResult[]; summary: ResultSummary }>
> {
  try {
    const { data } = await api.get<
      ApiResponse<{
        runId: string;
        results: TestResult[];
        summary: ResultSummary;
      }>
    >(`/api/tests/results/${runId}`);
    return data;
  } catch (err) {
    throw new Error(extractErrorMessage(err));
  }
}

/**
 * Download an Excel report for a run.
 * GET /api/tests/export/{runId}
 */
export async function exportReport(runId: string): Promise<Blob> {
  try {
    const { data } = await api.get<Blob>(`/api/tests/export/${runId}`, {
      responseType: "blob",
    });
    return data;
  } catch (err) {
    throw new Error(extractErrorMessage(err));
  }
}

/**
 * Get AI-generated insights for a failed test.
 * POST /api/insights/explain
 */
export async function explainFailure(
  request: InsightExplainRequest,
): Promise<ApiResponse<InsightExplainResponse>> {
  try {
    const { data } = await api.post<ApiResponse<InsightExplainResponse>>(
      "/api/insights/explain",
      request,
    );
    return data;
  } catch (err) {
    throw new Error(extractErrorMessage(err));
  }
}

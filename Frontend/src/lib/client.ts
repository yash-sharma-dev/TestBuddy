import axios, { AxiosError } from "axios";

// ── Types ────────────────────────────────────────────────────────────────────

export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH";

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: string;
}

export interface ParsedSpec {
  runId: string;
  title: string;
  endpointCount: number;
  endpoints: string[];
  [key: string]: unknown;
}

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
  schemaValid: boolean | null;
}

export interface ResultSummary {
  total: number;
  passed: number;
  failed: number;
  avgResponseTime: number;
}

export interface GenerateTestsParams {
  runId: string;
  instructions: string;
  targetBaseUrl: string;
  environment: string;
  authType: string;
  authValue: string;
}

export interface RunTestsParams {
  runId: string;
  testCases: TestCaseDto[];
  targetBaseUrl: string;
  authType: string;
  authValue: string;
}

export interface InsightExplainRequest {
  testCaseName: string;
  endpoint: string;
  method: string;
  payload: Record<string, unknown> | null;
  expectedStatus: number;
  actualStatus: number;
  errorMessage: string | null;
}

export interface InsightExplainResponse {
  technical: string;
  human: string;
  suggestion: string;
}

// ── Axios instance ───────────────────────────────────────────────────────────

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
  timeout: 30_000,
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ── Error utility ────────────────────────────────────────────────────────────

export function extractErrorMessage(error: unknown): string {
  if (error instanceof AxiosError) {
    const data = error.response?.data as Record<string, unknown> | undefined;
    if (typeof data?.message === "string") return data.message;
    if (typeof data?.error === "string") return data.error;
    if (error.response) return `Server responded with ${error.response.status}`;
    if (error.request) return "No response from server — is the backend running?";
    return error.message;
  }
  if (error instanceof Error) return error.message;
  return "An unexpected error occurred";
}

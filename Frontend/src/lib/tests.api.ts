import { apiClient, extractErrorMessage } from "./client";
import type {
  ApiResponse,
  TestCaseDto,
  TestResult,
  ResultSummary,
  GenerateTestsParams,
  RunTestsParams,
} from "./client";

export async function generateTests(
  specContent: string,
  params: GenerateTestsParams,
): Promise<TestCaseDto[]> {
  try {
    const { data } = await apiClient.post<ApiResponse<TestCaseDto[]>>(
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
      throw new Error(data.error ?? "Failed to generate tests");
    }

    return data.data ?? [];
  } catch (err) {
    throw new Error(extractErrorMessage(err));
  }
}

export async function runTests(
  params: RunTestsParams,
): Promise<ApiResponse<{ runId: string; results: TestResult[] }>> {
  try {
    const { data } = await apiClient.post<
      ApiResponse<{ runId: string; results: TestResult[] }>
    >("/api/tests/run", params);
    return data;
  } catch (err) {
    throw new Error(extractErrorMessage(err));
  }
}

export async function getResults(
  runId: string,
): Promise<
  ApiResponse<{ runId: string; results: TestResult[]; summary: ResultSummary }>
> {
  try {
    const { data } = await apiClient.get<
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

export async function exportReport(runId: string): Promise<Blob> {
  try {
    const { data } = await apiClient.get<Blob>(`/api/tests/export/${runId}`, {
      responseType: "blob",
    });
    return data;
  } catch (err) {
    throw new Error(extractErrorMessage(err));
  }
}

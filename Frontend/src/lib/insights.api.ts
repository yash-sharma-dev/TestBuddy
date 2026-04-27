import { apiClient, extractErrorMessage } from "./client";
import type { ApiResponse, InsightExplainRequest, InsightExplainResponse } from "./client";

export async function explainFailure(
  request: InsightExplainRequest,
): Promise<ApiResponse<InsightExplainResponse>> {
  try {
    const { data } = await apiClient.post<ApiResponse<InsightExplainResponse>>(
      "/api/insights/explain",
      request,
    );
    return data;
  } catch (err) {
    throw new Error(extractErrorMessage(err));
  }
}

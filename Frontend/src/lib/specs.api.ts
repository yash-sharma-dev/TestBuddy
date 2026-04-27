import { apiClient, extractErrorMessage } from "./client";
import type { ApiResponse, ParsedSpec } from "./client";

export async function uploadSpec(
  file: File,
  environment: string,
): Promise<ApiResponse<ParsedSpec>> {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("environment", environment);

  try {
    const { data } = await apiClient.post<ApiResponse<ParsedSpec>>(
      "/api/spec/upload",
      formData,
      { headers: { "Content-Type": "multipart/form-data" } },
    );
    return data;
  } catch (err) {
    throw new Error(extractErrorMessage(err));
  }
}

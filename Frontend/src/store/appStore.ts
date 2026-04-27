import { create } from "zustand";
import { persist } from "zustand/middleware";
import type {
  ParsedSpec,
  TestCaseDto,
  TestResult,
  ResultSummary,
} from "@/lib/api";

export type Environment = "dev" | "staging" | "prod";
export type AuthType = "NONE" | "JWT" | "API_KEY";

interface AppState {
  // ── Current run ────────────────────────────────────────────────────────
  runId: string | null;
  specContent: string | null;
  parsedSpec: ParsedSpec | null;

  // ── Test cases from generation ─────────────────────────────────────────
  testCases: TestCaseDto[];

  // ── Results after execution ────────────────────────────────────────────
  results: TestResult[];
  summary: ResultSummary | null;

  // ── Shared config across pages ─────────────────────────────────────────
  environment: Environment;
  targetBaseUrl: string;
  instructions: string;
  authType: AuthType;
  authValue: string;

  // ── UI loading flags ───────────────────────────────────────────────────
  isGenerating: boolean;
  isRunning: boolean;
  isExporting: boolean;

  // ── Session ────────────────────────────────────────────────────────────
  isAuthenticated: boolean;
  userEmail: string;
  authToken: string | null;

  // ── Actions ────────────────────────────────────────────────────────────
  setRunId: (runId: string | null) => void;
  setSpecContent: (content: string | null) => void;
  setParsedSpec: (spec: ParsedSpec | null) => void;
  setTestCases: (cases: TestCaseDto[]) => void;
  setResults: (results: TestResult[]) => void;
  setSummary: (summary: ResultSummary | null) => void;
  setEnvironment: (env: Environment) => void;
  setTargetBaseUrl: (url: string) => void;
  setInstructions: (instructions: string) => void;
  setAuthType: (type: AuthType) => void;
  setAuthValue: (value: string) => void;
  setGenerating: (v: boolean) => void;
  setRunning: (v: boolean) => void;
  setExporting: (v: boolean) => void;
  setSession: (email: string, token: string) => void;
  reset: () => void;
}

const workspaceDefaults = {
  runId: null as string | null,
  specContent: null as string | null,
  parsedSpec: null as ParsedSpec | null,
  testCases: [] as TestCaseDto[],
  results: [] as TestResult[],
  summary: null as ResultSummary | null,
  environment: "dev" as Environment,
  targetBaseUrl: "https://api.example.com",
  instructions: "",
  authType: "NONE" as AuthType,
  authValue: "",
  isGenerating: false,
  isRunning: false,
  isExporting: false,
};

const signedOutSession = {
  isAuthenticated: false,
  userEmail: "",
  authToken: null as string | null,
};

const initialState = {
  ...workspaceDefaults,
  ...signedOutSession,
};

export const useAppStore = create<AppState>()(
  persist(
    (set) => ({
      ...initialState,

      setRunId: (runId) => set({ runId }),
      setSpecContent: (specContent) => set({ specContent }),
      setParsedSpec: (parsedSpec) => set({ parsedSpec }),
      setTestCases: (testCases) => set({ testCases }),
      setResults: (results) => set({ results }),
      setSummary: (summary) => set({ summary }),
      setEnvironment: (environment) => set({ environment }),
      setTargetBaseUrl: (targetBaseUrl) => set({ targetBaseUrl }),
      setInstructions: (instructions) => set({ instructions }),
      setAuthType: (authType) => set({ authType }),
      setAuthValue: (authValue) => set({ authValue }),
      setGenerating: (isGenerating) => set({ isGenerating }),
      setRunning: (isRunning) => set({ isRunning }),
      setExporting: (isExporting) => set({ isExporting }),
      setSession: (userEmail, token) => {
        localStorage.setItem("token", token);
        set({ isAuthenticated: true, userEmail, authToken: token });
      },
      reset: () => {
        localStorage.removeItem("token");
        set({ ...workspaceDefaults, ...signedOutSession });
      },
    }),
    {
      name: "testbuddy-app-v3",
      partialize: (state) => ({
        isAuthenticated: state.isAuthenticated,
        userEmail: state.userEmail,
        authToken: state.authToken,
      }),
    },
  ),
);

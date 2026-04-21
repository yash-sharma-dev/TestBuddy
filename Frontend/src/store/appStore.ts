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

  // ── Session (UI; wire to real auth when backend exists) ─────────────────
  isAuthenticated: boolean;
  userEmail: string;

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
  setSession: (email: string) => void;
  reset: () => void;
}

/** Workspace fields reset on sign-out and when clearing a run (via reset). */
const workspaceDefaults = {
  runId: null,
  specContent: null,
  parsedSpec: null,
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

/** Fresh app load: signed-in demo user so the header matches the product shell (bell + profile menu). */
const demoSession = {
  isAuthenticated: true,
  userEmail: "jane@testbuddy.dev",
};

const signedOutSession = {
  isAuthenticated: false,
  userEmail: "",
};

const initialState = {
  ...workspaceDefaults,
  ...demoSession,
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
      setSession: (userEmail) => set({ isAuthenticated: true, userEmail }),
      /** Full reset: clear workspace and sign out (does not restore demo session). */
      reset: () =>
        set({
          ...workspaceDefaults,
          ...signedOutSession,
        }),
    }),
    {
      // Bump when default session shape changes so users aren’t stuck signed-out with no profile UI.
      name: "testbuddy-app-v2",
      partialize: (state) => ({
        isAuthenticated: state.isAuthenticated,
        userEmail: state.userEmail,
      }),
    },
  ),
);

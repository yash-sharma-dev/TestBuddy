import { useState, useEffect, useMemo, useCallback } from "react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { MethodBadge } from "@/components/testbuddy/MethodBadge";
import { Sparkles, AlertCircle, Lightbulb, Code2, Send, Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";
import { toast } from "sonner";
import { useAppStore } from "@/store/appStore";
import { explainFailure } from "@/lib/api";
import type { TestResult, InsightExplainResponse } from "@/lib/api";
import type { HttpMethod } from "@/data/mock";

export default function Insights() {
  const { results, testCases } = useAppStore();

  // Get failed results with their test case info
  const failedResults = useMemo(() => {
    return results.filter((r) => !r.passed);
  }, [results]);

  const [selectedId, setSelectedId] = useState<string | null>(failedResults[0]?.testCaseId ?? null);
  const [insights, setInsights] = useState<Record<string, InsightExplainResponse>>({});
  const [loadingInsight, setLoadingInsight] = useState<string | null>(null);

  const selected = failedResults.find((r) => r.testCaseId === selectedId);

  // Fetch insight for the selected test
  const fetchInsight = useCallback(async (result: TestResult) => {
    if (insights[result.testCaseId]) return; // already fetched

    setLoadingInsight(result.testCaseId);
    try {
      const tc = testCases.find((t) => t.id === result.testCaseId);
      const response = await explainFailure({
        testCaseName: result.name,
        endpoint: result.endpoint,
        method: result.method,
        payload: tc?.payload ?? result.payload,
        expectedStatus: result.expectedStatus,
        actualStatus: result.actualStatus,
        errorMessage: result.errorMessage,
      });
      setInsights((prev) => ({ ...prev, [result.testCaseId]: response.data }));
    } catch {
      // Backend endpoint may not exist yet — show a fallback insight
      const fallbackInsight: InsightExplainResponse = {
        rootCause: `The test expected status ${result.expectedStatus} but received ${result.actualStatus}. ${result.errorMessage ?? "No additional error details available from the server."}`,
        suggestion: `Verify the endpoint ${result.endpoint} is correctly configured and the request payload matches the API contract. Check server logs for more details.`,
        snippet: `// Expected: ${result.expectedStatus}\n// Actual: ${result.actualStatus}\n// Endpoint: ${result.method} ${result.endpoint}`,
      };
      setInsights((prev) => ({ ...prev, [result.testCaseId]: fallbackInsight }));
      toast.error("AI insights endpoint not available — showing basic analysis");
    } finally {
      setLoadingInsight(null);
    }
  }, [insights, testCases]);

  // Auto-fetch insight when selection changes
  useEffect(() => {
    if (selected) {
      fetchInsight(selected);
    }
  }, [selected, fetchInsight]);

  const insight = selected ? insights[selected.testCaseId] : null;

  // Empty state
  if (failedResults.length === 0) {
    return (
      <div className="space-y-6">
        <div className="space-y-1">
          <h1 className="flex items-center gap-2 text-2xl font-bold tracking-tight">
            <Sparkles className="h-6 w-6 text-primary" /> AI Insights
          </h1>
          <p className="text-sm text-muted-foreground">Smart explanations and fixes for every failure.</p>
        </div>
        <Card className="flex flex-col items-center justify-center gap-4 p-12 shadow-card">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-success/10">
            <Sparkles className="h-7 w-7 text-success" />
          </div>
          <p className="text-center text-sm text-muted-foreground">
            {results.length === 0
              ? "Run your test cases first to see AI insights for failures."
              : "All tests passed — no failures to analyze!"}
          </p>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h1 className="flex items-center gap-2 text-2xl font-bold tracking-tight">
          <Sparkles className="h-6 w-6 text-primary" /> AI Insights
        </h1>
        <p className="text-sm text-muted-foreground">Smart explanations and fixes for every failure.</p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Failed tests list */}
        <Card className="p-3 shadow-card lg:col-span-1">
          <p className="px-2 py-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
            Failed tests · {failedResults.length}
          </p>
          <ul className="space-y-1">
            {failedResults.map((r) => (
              <li key={r.testCaseId}>
                <button
                  onClick={() => setSelectedId(r.testCaseId)}
                  className={cn(
                    "w-full rounded-lg p-3 text-left transition-all",
                    selectedId === r.testCaseId ? "bg-primary/10 ring-1 ring-primary/20" : "hover:bg-muted/60",
                  )}
                >
                  <div className="mb-1 flex items-center gap-2">
                    <MethodBadge method={r.method as HttpMethod} />
                    <span className="text-[10px] font-mono text-destructive">{r.actualStatus}</span>
                  </div>
                  <p className="text-sm font-medium leading-tight">{r.name}</p>
                  <p className="mt-0.5 truncate font-mono text-[11px] text-muted-foreground">{r.endpoint}</p>
                </button>
              </li>
            ))}
          </ul>
        </Card>

        {/* Chat panel */}
        <Card className="flex flex-col shadow-card lg:col-span-2">
          {selected && insight ? (
            <>
              <div className="flex items-start gap-3 border-b p-5">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl gradient-primary shadow-glow">
                  <Sparkles className="h-5 w-5 text-primary-foreground" />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-xs font-medium text-primary">TestBuddy AI</p>
                  <p className="text-base font-semibold">{selected.name}</p>
                  <p className="truncate font-mono text-xs text-muted-foreground">{selected.method} {selected.endpoint}</p>
                </div>
              </div>

              <div className="space-y-5 overflow-auto p-5">
                <Section icon={AlertCircle} tone="destructive" title="Root Cause">
                  <p>{insight.rootCause}</p>
                </Section>

                <Section icon={Lightbulb} tone="warning" title="Suggested Fix">
                  <p>{insight.suggestion}</p>
                </Section>

                <Section icon={Code2} tone="primary" title="Code Snippet">
                  <pre className="overflow-auto rounded-lg bg-foreground/95 p-4 text-xs leading-relaxed text-background">
                    <code>{insight.snippet}</code>
                  </pre>
                </Section>
              </div>

              <form
                onSubmit={(e) => { e.preventDefault(); toast.success("AI is thinking… (follow-up coming soon)"); }}
                className="flex items-center gap-2 border-t p-4"
              >
                <Input placeholder="Ask a follow-up question…" className="flex-1" />
                <Button type="submit" className="gradient-primary"><Send className="h-4 w-4" /></Button>
              </form>
            </>
          ) : selected && loadingInsight === selected.testCaseId ? (
            <div className="flex flex-1 items-center justify-center p-12">
              <Loader2 className="h-6 w-6 animate-spin text-primary" />
              <span className="ml-2 text-sm text-muted-foreground">Analyzing failure…</span>
            </div>
          ) : (
            <div className="flex flex-1 items-center justify-center p-12 text-sm text-muted-foreground">
              Select a failed test to view AI analysis.
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}

const tones = {
  destructive: "bg-destructive/10 text-destructive",
  warning: "bg-warning/10 text-warning",
  primary: "bg-primary/10 text-primary",
};

function Section({
  icon: Icon,
  tone,
  title,
  children,
}: {
  icon: typeof AlertCircle;
  tone: keyof typeof tones;
  title: string;
  children: React.ReactNode;
}) {
  return (
    <div className="animate-fade-in space-y-2">
      <div className="flex items-center gap-2">
        <div className={cn("flex h-7 w-7 items-center justify-center rounded-lg", tones[tone])}>
          <Icon className="h-3.5 w-3.5" />
        </div>
        <h3 className="text-sm font-semibold">{title}</h3>
      </div>
      <div className="pl-9 text-sm leading-relaxed text-muted-foreground">{children}</div>
    </div>
  );
}

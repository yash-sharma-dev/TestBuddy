import { useState, useMemo } from "react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { MethodBadge } from "@/components/testbuddy/MethodBadge";
import { StatusBadge } from "@/components/testbuddy/StatusBadge";
import type { TestStatus, HttpMethod } from "@/data/mock";
import { Play, Search, Eye, Loader2, Upload } from "lucide-react";
import { toast } from "sonner";
import { Link, useNavigate } from "react-router-dom";
import { useAppStore } from "@/store/appStore";
import { runTests } from "@/lib/api";
import type { TestCaseDto } from "@/lib/api";

type Filter = "all" | "passed" | "failed" | "not_run";

/** Map a TestCaseDto + optional result to the UI shape expected by the table. */
function mapStatus(tc: TestCaseDto, results: ReturnType<typeof useAppStore.getState>["results"]): TestStatus {
  const result = results.find((r) => r.testCaseId === tc.id);
  if (!result) return "not_run";
  return result.passed ? "passed" : "failed";
}

function getResponseCode(tc: TestCaseDto, results: ReturnType<typeof useAppStore.getState>["results"]): number | undefined {
  return results.find((r) => r.testCaseId === tc.id)?.actualStatus;
}

function getResponseTime(tc: TestCaseDto, results: ReturnType<typeof useAppStore.getState>["results"]): number | undefined {
  return results.find((r) => r.testCaseId === tc.id)?.responseTimeMs;
}

export default function TestCases() {
  const [filter, setFilter] = useState<Filter>("all");
  const [query, setQuery] = useState("");
  const [runningSingle, setRunningSingle] = useState<string | null>(null);
  const navigate = useNavigate();

  const {
    testCases,
    results,
    runId,
    targetBaseUrl,
    authType,
    authValue,
    isRunning,
    setRunning,
    setResults,
    setSummary,
  } = useAppStore();

  const filtered = useMemo(() => {
    return testCases.filter((tc) => {
      const status = mapStatus(tc, results);
      const matchesFilter = filter === "all" || status === filter;
      const matchesQuery =
        !query ||
        tc.name.toLowerCase().includes(query.toLowerCase()) ||
        tc.endpoint.toLowerCase().includes(query.toLowerCase());
      return matchesFilter && matchesQuery;
    });
  }, [testCases, results, filter, query]);

  const runAll = async () => {
    if (!runId) {
      toast.error("No active run — please upload a spec first.");
      return;
    }
    setRunning(true);
    try {
      const response = await runTests({
        runId,
        testCases,
        targetBaseUrl,
        authType,
        authValue,
      });
      setResults(response.data.results);

      const passed = response.data.results.filter((r) => r.passed).length;
      const failed = response.data.results.filter((r) => !r.passed).length;
      toast.success(`${passed} passed, ${failed} failed`);
      navigate("/results");
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to run tests";
      toast.error(message);
    } finally {
      setRunning(false);
    }
  };

  const runSingle = async (tc: TestCaseDto) => {
    if (!runId) {
      toast.error("No active run — please upload a spec first.");
      return;
    }
    setRunningSingle(tc.id);
    try {
      const response = await runTests({
        runId,
        testCases: [tc],
        targetBaseUrl,
        authType,
        authValue,
      });
      // Merge single result into existing results
      const newResult = response.data.results[0];
      if (newResult) {
        const updated = [...results.filter((r) => r.testCaseId !== tc.id), newResult];
        setResults(updated);
        toast.success(newResult.passed ? `${tc.name} — passed` : `${tc.name} — failed`);
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to run test";
      toast.error(message);
    } finally {
      setRunningSingle(null);
    }
  };

  // Empty state when no test cases exist
  if (testCases.length === 0) {
    return (
      <div className="space-y-6">
        <div className="space-y-1">
          <h1 className="text-2xl font-bold tracking-tight">Test Cases</h1>
          <p className="text-sm text-muted-foreground">No test cases generated yet</p>
        </div>
        <Card className="flex flex-col items-center justify-center gap-4 p-12 shadow-card">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/10">
            <Upload className="h-7 w-7 text-primary" />
          </div>
          <p className="text-center text-sm text-muted-foreground">
            Upload an API spec to generate test cases automatically.
          </p>
          <Button asChild className="gradient-primary shadow-glow">
            <Link to="/upload">Upload API Spec</Link>
          </Button>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div className="space-y-1">
          <h1 className="text-2xl font-bold tracking-tight">Test Cases</h1>
          <p className="text-sm text-muted-foreground">{testCases.length} tests generated from your latest spec</p>
        </div>
        <Button onClick={runAll} disabled={isRunning} size="lg" className="gradient-primary shadow-glow hover:opacity-95">
          {isRunning ? <><Loader2 className="h-4 w-4 animate-spin" /> Running…</> : <><Play className="h-4 w-4" /> Run All Tests</>}
        </Button>
      </div>

      <Card className="overflow-hidden shadow-card">
        <div className="flex flex-col gap-3 border-b p-4 md:flex-row md:items-center md:justify-between">
          <Tabs value={filter} onValueChange={(v) => setFilter(v as Filter)}>
            <TabsList>
              <TabsTrigger value="all">All</TabsTrigger>
              <TabsTrigger value="passed">Passed</TabsTrigger>
              <TabsTrigger value="failed">Failed</TabsTrigger>
              <TabsTrigger value="not_run">Not run</TabsTrigger>
            </TabsList>
          </Tabs>
          <div className="relative w-full md:w-72">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search tests…" className="pl-9" />
          </div>
        </div>

        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/40 hover:bg-muted/40">
                <TableHead>Test Name</TableHead>
                <TableHead>Method</TableHead>
                <TableHead>Endpoint</TableHead>
                <TableHead>Payload</TableHead>
                <TableHead>Expected</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filtered.map((tc) => (
                <TestRow
                  key={tc.id}
                  tc={tc}
                  status={mapStatus(tc, results)}
                  responseCode={getResponseCode(tc, results)}
                  isRunningSingle={runningSingle === tc.id}
                  onRun={() => runSingle(tc)}
                />
              ))}
              {filtered.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} className="py-12 text-center text-sm text-muted-foreground">No test cases match your filters.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
      </Card>
    </div>
  );
}

function TestRow({
  tc,
  status,
  responseCode: _responseCode,
  isRunningSingle,
  onRun,
}: {
  tc: TestCaseDto;
  status: TestStatus;
  responseCode: number | undefined;
  isRunningSingle: boolean;
  onRun: () => void;
}) {
  return (
    <TableRow className="hover:bg-muted/40">
      <TableCell className="font-medium">{tc.name}</TableCell>
      <TableCell><MethodBadge method={tc.method as HttpMethod} /></TableCell>
      <TableCell className="font-mono text-xs text-muted-foreground">{tc.endpoint}</TableCell>
      <TableCell>
        {tc.payload ? (
          <Dialog>
            <DialogTrigger asChild>
              <Button variant="ghost" size="sm" className="h-7 gap-1 text-xs">
                <Eye className="h-3 w-3" /> View
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Request Payload</DialogTitle>
                <DialogDescription>{tc.method} {tc.endpoint}</DialogDescription>
              </DialogHeader>
              <pre className="max-h-96 overflow-auto rounded-lg bg-muted p-4 text-xs">
                {JSON.stringify(tc.payload, null, 2)}
              </pre>
            </DialogContent>
          </Dialog>
        ) : (
          <span className="text-xs text-muted-foreground">—</span>
        )}
      </TableCell>
      <TableCell><span className="rounded-md bg-muted px-2 py-0.5 font-mono text-xs">{tc.expectedStatus}</span></TableCell>
      <TableCell><StatusBadge status={status} /></TableCell>
      <TableCell className="text-right">
        <Button
          variant="ghost"
          size="icon"
          className="h-8 w-8 text-primary hover:bg-primary/10"
          onClick={onRun}
          disabled={isRunningSingle}
        >
          {isRunningSingle ? (
            <Loader2 className="h-3.5 w-3.5 animate-spin" />
          ) : (
            <Play className="h-3.5 w-3.5" />
          )}
        </Button>
      </TableCell>
    </TableRow>
  );
}

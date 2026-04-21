import { useMemo } from "react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { StatusBadge } from "@/components/testbuddy/StatusBadge";
import { Bar, BarChart, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis, Legend } from "recharts";
import { Download, Loader2, Upload } from "lucide-react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { useAppStore } from "@/store/appStore";
import { exportReport } from "@/lib/api";
import type { TestStatus } from "@/data/mock";

const COLORS = ["hsl(var(--success))", "hsl(var(--destructive))"];

export default function Results() {
  const { results, runId, isExporting, setExporting } = useAppStore();

  const passedCount = useMemo(() => results.filter((r) => r.passed).length, [results]);
  const failedCount = useMemo(() => results.filter((r) => !r.passed).length, [results]);
  const totalCount = results.length;

  const pieData = useMemo(
    () => [
      { name: "Passed", value: passedCount },
      { name: "Failed", value: failedCount },
    ],
    [passedCount, failedCount],
  );

  const performanceData = useMemo(() => {
    // Group results by endpoint base path and compute average response time
    const groups: Record<string, { total: number; count: number }> = {};
    results.forEach((r) => {
      // Use just the path without query params, shortened
      const key = r.endpoint.split("?")[0].split("/").filter(Boolean).slice(-1)[0] || r.endpoint;
      if (!groups[key]) groups[key] = { total: 0, count: 0 };
      groups[key].total += r.responseTimeMs;
      groups[key].count += 1;
    });
    return Object.entries(groups).map(([endpoint, { total, count }]) => ({
      endpoint: `/${endpoint}`,
      time: Math.round(total / count),
    }));
  }, [results]);

  const handleExport = async () => {
    if (!runId) {
      toast.error("No run ID available for export.");
      return;
    }
    setExporting(true);
    try {
      const blob = await exportReport(runId);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `test-report-${runId}.xlsx`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      toast.success("Report downloaded successfully");
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to export report";
      toast.error(message);
    } finally {
      setExporting(false);
    }
  };

  // Empty state when no results exist
  if (results.length === 0) {
    return (
      <div className="space-y-6">
        <div className="space-y-1">
          <h1 className="text-2xl font-bold tracking-tight">Results Dashboard</h1>
          <p className="text-sm text-muted-foreground">No test results available yet</p>
        </div>
        <Card className="flex flex-col items-center justify-center gap-4 p-12 shadow-card">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/10">
            <Upload className="h-7 w-7 text-primary" />
          </div>
          <p className="text-center text-sm text-muted-foreground">
            Run your test cases to see results here.
          </p>
          <Button asChild className="gradient-primary shadow-glow">
            <Link to="/test-cases">Go to Test Cases</Link>
          </Button>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div className="space-y-1">
          <h1 className="text-2xl font-bold tracking-tight">Results Dashboard</h1>
          <p className="text-sm text-muted-foreground">Latest test execution from {new Date().toLocaleDateString()}</p>
        </div>
        <Button variant="outline" onClick={handleExport} disabled={isExporting}>
          {isExporting ? (
            <><Loader2 className="h-4 w-4 animate-spin" /> Exporting…</>
          ) : (
            <><Download className="h-4 w-4" /> Export report</>
          )}
        </Button>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
        <Card className="p-5 shadow-card lg:col-span-2">
          <h2 className="mb-1 text-base font-semibold">Pass vs Fail</h2>
          <p className="mb-3 text-xs text-muted-foreground">Out of {totalCount} tests</p>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={pieData} dataKey="value" innerRadius={55} outerRadius={90} paddingAngle={3} stroke="hsl(var(--background))" strokeWidth={3}>
                  {pieData.map((_, i) => <Cell key={i} fill={COLORS[i]} />)}
                </Pie>
                <Tooltip contentStyle={{ background: "hsl(var(--card))", border: "1px solid hsl(var(--border))", borderRadius: 12, fontSize: 12 }} />
                <Legend iconType="circle" wrapperStyle={{ fontSize: 12 }} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </Card>

        <Card className="p-5 shadow-card lg:col-span-3">
          <h2 className="mb-1 text-base font-semibold">Avg response time by endpoint</h2>
          <p className="mb-3 text-xs text-muted-foreground">Milliseconds</p>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={performanceData} margin={{ left: -16, right: 8, top: 4 }}>
                <XAxis dataKey="endpoint" stroke="hsl(var(--muted-foreground))" fontSize={11} tickLine={false} axisLine={false} />
                <YAxis stroke="hsl(var(--muted-foreground))" fontSize={11} tickLine={false} axisLine={false} />
                <Tooltip cursor={{ fill: "hsl(var(--muted))" }} contentStyle={{ background: "hsl(var(--card))", border: "1px solid hsl(var(--border))", borderRadius: 12, fontSize: 12 }} />
                <Bar dataKey="time" fill="hsl(var(--primary))" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      <Card className="overflow-hidden shadow-card">
        <div className="border-b p-4">
          <h2 className="text-base font-semibold">Execution details</h2>
        </div>
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/40 hover:bg-muted/40">
                <TableHead>Test Name</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Response</TableHead>
                <TableHead>Time</TableHead>
                <TableHead>Error</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {results.map((r) => {
                const status: TestStatus = r.passed ? "passed" : "failed";
                return (
                  <TableRow key={r.testCaseId} className="hover:bg-muted/40">
                    <TableCell className="font-medium">{r.name}</TableCell>
                    <TableCell><StatusBadge status={status} /></TableCell>
                    <TableCell>
                      <span className={`font-mono text-xs font-semibold ${r.actualStatus >= 400 ? "text-destructive" : "text-success"}`}>
                        {r.actualStatus}
                      </span>
                    </TableCell>
                    <TableCell className="text-xs text-muted-foreground">{r.responseTimeMs}ms</TableCell>
                    <TableCell className="max-w-md truncate text-xs text-muted-foreground" title={r.errorMessage ?? undefined}>
                      {r.errorMessage ?? "—"}
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </div>
      </Card>
    </div>
  );
}

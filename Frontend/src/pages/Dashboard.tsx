import { useMemo } from "react";
import { Activity, CheckCircle2, Network, XCircle, ArrowRight, Sparkles } from "lucide-react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { StatCard } from "@/components/testbuddy/StatCard";
import { MethodBadge } from "@/components/testbuddy/MethodBadge";
import { StatusBadge } from "@/components/testbuddy/StatusBadge";
import { trendData } from "@/data/mock";
import type { HttpMethod, TestStatus } from "@/data/mock";
import { Link } from "react-router-dom";
import { Area, AreaChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { useAppStore } from "@/store/appStore";

export default function Dashboard() {
  const { results, testCases, parsedSpec } = useAppStore();

  const stats = useMemo(() => {
    const totalTests = results.length || testCases.length;
    const passedTests = results.filter((r) => r.passed).length;
    const failedTests = results.filter((r) => !r.passed).length;
    const totalApis = parsedSpec?.endpointCount ?? new Set(testCases.map((tc) => tc.endpoint)).size;

    return {
      totalApis,
      totalTests,
      passedTests,
      failedTests,
      trends: {
        apis: totalApis > 0 ? `${totalApis} endpoints` : "—",
        tests: totalTests > 0 ? `${totalTests} total` : "—",
        passed: passedTests > 0 ? `${Math.round((passedTests / (totalTests || 1)) * 100)}%` : "—",
        failed: failedTests > 0 ? `${failedTests} issues` : "—",
      },
    };
  }, [results, testCases, parsedSpec]);

  const recentActivity = useMemo(() => {
    // Take the last 6 results and map to the activity shape
    return results.slice(-6).reverse().map((r, i) => ({
      id: r.testCaseId,
      name: r.name,
      endpoint: r.endpoint,
      method: r.method as HttpMethod,
      status: (r.passed ? "passed" : "failed") as TestStatus,
      time: `${r.responseTimeMs}ms`,
      duration: `${r.responseTimeMs}ms`,
      _index: i,
    }));
  }, [results]);

  return (
    <div className="space-y-6">
      {/* Hero */}
      <Card className="relative overflow-hidden border-0 p-6 shadow-card-lg md:p-8">
        <div className="absolute inset-0 gradient-hero" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,_rgba(255,255,255,0.25),_transparent_50%)]" />
        <div className="relative flex flex-col items-start gap-4 text-primary-foreground md:flex-row md:items-center md:justify-between">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-1.5 rounded-full bg-white/15 px-3 py-1 text-xs font-medium backdrop-blur-sm">
              <Sparkles className="h-3 w-3" /> AI-powered
            </div>
            <h1 className="text-2xl font-bold tracking-tight md:text-3xl">
              AI API Test Automation with TestBuddy
            </h1>
            <p className="max-w-xl text-sm text-primary-foreground/85 md:text-base">
              Upload your OpenAPI spec, generate intelligent test cases, and let AI explain every failure.
            </p>
          </div>
          <Button asChild size="lg" variant="secondary" className="bg-white text-primary hover:bg-white/90">
            <Link to="/upload">
              Upload API Spec <ArrowRight className="ml-1 h-4 w-4" />
            </Link>
          </Button>
        </div>
      </Card>

      {/* Stats */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Total APIs" value={stats.totalApis} trend={stats.trends.apis} icon={Network} tone="primary" />
        <StatCard label="Total Tests" value={stats.totalTests} trend={stats.trends.tests} icon={Activity} tone="accent" />
        <StatCard label="Passed Tests" value={stats.passedTests} trend={stats.trends.passed} icon={CheckCircle2} tone="success" />
        <StatCard label="Failed Tests" value={stats.failedTests} trend={stats.trends.failed} icon={XCircle} tone="destructive" />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
        {/* Trend chart */}
        <Card className="p-5 shadow-card lg:col-span-3">
          <div className="mb-4 flex items-center justify-between">
            <div>
              <h2 className="text-base font-semibold">Test runs · last 7 days</h2>
              <p className="text-xs text-muted-foreground">Total runs vs passed</p>
            </div>
          </div>
          <div className="h-56">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={trendData} margin={{ left: -20, right: 8, top: 4 }}>
                <defs>
                  <linearGradient id="runs" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="hsl(var(--primary))" stopOpacity={0.3} />
                    <stop offset="100%" stopColor="hsl(var(--primary))" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="passed" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="hsl(var(--success))" stopOpacity={0.3} />
                    <stop offset="100%" stopColor="hsl(var(--success))" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <XAxis dataKey="day" stroke="hsl(var(--muted-foreground))" fontSize={11} tickLine={false} axisLine={false} />
                <YAxis stroke="hsl(var(--muted-foreground))" fontSize={11} tickLine={false} axisLine={false} />
                <Tooltip
                  contentStyle={{
                    background: "hsl(var(--card))",
                    border: "1px solid hsl(var(--border))",
                    borderRadius: 12,
                    fontSize: 12,
                  }}
                />
                <Area type="monotone" dataKey="runs" stroke="hsl(var(--primary))" strokeWidth={2} fill="url(#runs)" />
                <Area type="monotone" dataKey="passed" stroke="hsl(var(--success))" strokeWidth={2} fill="url(#passed)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Activity */}
        <Card className="p-5 shadow-card lg:col-span-2">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-base font-semibold">Recent activity</h2>
            <Link to="/results" className="text-xs font-medium text-primary hover:underline">View all</Link>
          </div>
          <ul className="space-y-1">
            {recentActivity.length > 0 ? (
              recentActivity.map((item) => (
                <li key={`${item.id}-${item._index}`} className="group flex items-center gap-3 rounded-lg p-2 transition-colors hover:bg-muted/60">
                  <MethodBadge method={item.method} />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium">{item.name}</p>
                    <p className="truncate text-xs text-muted-foreground">{item.endpoint}</p>
                  </div>
                  <div className="flex flex-col items-end gap-0.5">
                    <StatusBadge status={item.status} />
                    <span className="text-[10px] text-muted-foreground">{item.time}</span>
                  </div>
                </li>
              ))
            ) : (
              <li className="py-8 text-center text-sm text-muted-foreground">
                Run tests to see recent activity here.
              </li>
            )}
          </ul>
        </Card>
      </div>
    </div>
  );
}

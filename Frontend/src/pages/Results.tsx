import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { StatusBadge } from "@/components/testbuddy/StatusBadge";
import { stats, testCases, performanceData } from "@/data/mock";
import { Bar, BarChart, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis, Legend } from "recharts";
import { Download } from "lucide-react";

const COLORS = ["hsl(var(--success))", "hsl(var(--destructive))"];

export default function Results() {
  const pieData = [
    { name: "Passed", value: stats.passedTests },
    { name: "Failed", value: stats.failedTests },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div className="space-y-1">
          <h1 className="text-2xl font-bold tracking-tight">Results Dashboard</h1>
          <p className="text-sm text-muted-foreground">Latest test execution from {new Date().toLocaleDateString()}</p>
        </div>
        <Button variant="outline"><Download className="h-4 w-4" /> Export report</Button>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
        <Card className="p-5 shadow-card lg:col-span-2">
          <h2 className="mb-1 text-base font-semibold">Pass vs Fail</h2>
          <p className="mb-3 text-xs text-muted-foreground">Out of {stats.totalTests} tests</p>
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
              {testCases.map((tc) => (
                <TableRow key={tc.id} className="hover:bg-muted/40">
                  <TableCell className="font-medium">{tc.name}</TableCell>
                  <TableCell><StatusBadge status={tc.status} /></TableCell>
                  <TableCell>
                    <span className={`font-mono text-xs font-semibold ${tc.responseCode && tc.responseCode >= 400 ? "text-destructive" : "text-success"}`}>
                      {tc.responseCode ?? "—"}
                    </span>
                  </TableCell>
                  <TableCell className="text-xs text-muted-foreground">{tc.responseTime ? `${tc.responseTime}ms` : "—"}</TableCell>
                  <TableCell className="max-w-md truncate text-xs text-muted-foreground" title={tc.errorMessage}>
                    {tc.errorMessage ?? "—"}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </Card>
    </div>
  );
}

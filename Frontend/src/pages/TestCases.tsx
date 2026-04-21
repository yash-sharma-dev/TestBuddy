import { useState, useMemo } from "react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { MethodBadge } from "@/components/testbuddy/MethodBadge";
import { StatusBadge } from "@/components/testbuddy/StatusBadge";
import { testCases, type TestCase } from "@/data/mock";
import { Play, Search, Eye, Loader2 } from "lucide-react";
import { toast } from "sonner";

type Filter = "all" | "passed" | "failed" | "not_run";

export default function TestCases() {
  const [filter, setFilter] = useState<Filter>("all");
  const [query, setQuery] = useState("");
  const [running, setRunning] = useState(false);

  const filtered = useMemo(() => {
    return testCases.filter((tc) => {
      const matchesFilter = filter === "all" || tc.status === filter;
      const matchesQuery = !query || tc.name.toLowerCase().includes(query.toLowerCase()) || tc.endpoint.toLowerCase().includes(query.toLowerCase());
      return matchesFilter && matchesQuery;
    });
  }, [filter, query]);

  const runAll = () => {
    setRunning(true);
    setTimeout(() => {
      setRunning(false);
      toast.success(`Executed ${testCases.length} tests · ${testCases.filter(t => t.status === "passed").length} passed`);
    }, 1600);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div className="space-y-1">
          <h1 className="text-2xl font-bold tracking-tight">Test Cases</h1>
          <p className="text-sm text-muted-foreground">{testCases.length} tests generated from your latest spec</p>
        </div>
        <Button onClick={runAll} disabled={running} size="lg" className="gradient-primary shadow-glow hover:opacity-95">
          {running ? <><Loader2 className="h-4 w-4 animate-spin" /> Running…</> : <><Play className="h-4 w-4" /> Run All Tests</>}
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
                <TestRow key={tc.id} tc={tc} />
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

function TestRow({ tc }: { tc: TestCase }) {
  return (
    <TableRow className="hover:bg-muted/40">
      <TableCell className="font-medium">{tc.name}</TableCell>
      <TableCell><MethodBadge method={tc.method} /></TableCell>
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
      <TableCell><StatusBadge status={tc.status} /></TableCell>
      <TableCell className="text-right">
        <Button variant="ghost" size="icon" className="h-8 w-8 text-primary hover:bg-primary/10">
          <Play className="h-3.5 w-3.5" />
        </Button>
      </TableCell>
    </TableRow>
  );
}

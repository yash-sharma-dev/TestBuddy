import { useState } from "react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { MethodBadge } from "@/components/testbuddy/MethodBadge";
import { testCases, aiInsights } from "@/data/mock";
import { Sparkles, AlertCircle, Lightbulb, Code2, Send } from "lucide-react";
import { cn } from "@/lib/utils";
import { toast } from "sonner";

export default function Insights() {
  const failed = testCases.filter((t) => t.status === "failed" && aiInsights[t.id]);
  const [selectedId, setSelectedId] = useState(failed[0]?.id);
  const selected = failed.find((t) => t.id === selectedId);
  const insight = selected ? aiInsights[selected.id] : null;

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
            Failed tests · {failed.length}
          </p>
          <ul className="space-y-1">
            {failed.map((t) => (
              <li key={t.id}>
                <button
                  onClick={() => setSelectedId(t.id)}
                  className={cn(
                    "w-full rounded-lg p-3 text-left transition-all",
                    selectedId === t.id ? "bg-primary/10 ring-1 ring-primary/20" : "hover:bg-muted/60",
                  )}
                >
                  <div className="mb-1 flex items-center gap-2">
                    <MethodBadge method={t.method} />
                    <span className="text-[10px] font-mono text-destructive">{t.responseCode}</span>
                  </div>
                  <p className="text-sm font-medium leading-tight">{t.name}</p>
                  <p className="mt-0.5 truncate font-mono text-[11px] text-muted-foreground">{t.endpoint}</p>
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
                onSubmit={(e) => { e.preventDefault(); toast.success("AI is thinking… (mock response)"); }}
                className="flex items-center gap-2 border-t p-4"
              >
                <Input placeholder="Ask a follow-up question…" className="flex-1" />
                <Button type="submit" className="gradient-primary"><Send className="h-4 w-4" /></Button>
              </form>
            </>
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

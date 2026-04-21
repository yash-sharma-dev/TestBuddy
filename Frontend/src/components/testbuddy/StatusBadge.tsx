import { Check, X, Clock } from "lucide-react";
import { cn } from "@/lib/utils";
import type { TestStatus } from "@/data/mock";

export function StatusBadge({ status, className }: { status: TestStatus; className?: string }) {
  if (status === "passed") {
    return (
      <span className={cn("inline-flex items-center gap-1 rounded-full bg-success/10 px-2.5 py-0.5 text-xs font-medium text-success", className)}>
        <Check className="h-3 w-3" /> Passed
      </span>
    );
  }
  if (status === "failed") {
    return (
      <span className={cn("inline-flex items-center gap-1 rounded-full bg-destructive/10 px-2.5 py-0.5 text-xs font-medium text-destructive", className)}>
        <X className="h-3 w-3" /> Failed
      </span>
    );
  }
  return (
    <span className={cn("inline-flex items-center gap-1 rounded-full bg-muted px-2.5 py-0.5 text-xs font-medium text-muted-foreground", className)}>
      <Clock className="h-3 w-3" /> Not run
    </span>
  );
}

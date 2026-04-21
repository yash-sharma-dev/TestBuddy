import { cn } from "@/lib/utils";
import type { HttpMethod } from "@/data/mock";

const styles: Record<HttpMethod, string> = {
  GET: "bg-primary/10 text-primary",
  POST: "bg-success/10 text-success",
  PUT: "bg-warning/10 text-warning",
  PATCH: "bg-accent/10 text-accent",
  DELETE: "bg-destructive/10 text-destructive",
};

export function MethodBadge({ method, className }: { method: HttpMethod; className?: string }) {
  return (
    <span
      className={cn(
        "inline-flex h-6 min-w-[52px] items-center justify-center rounded-md px-2 text-[11px] font-bold tracking-wide",
        styles[method],
        className,
      )}
    >
      {method}
    </span>
  );
}

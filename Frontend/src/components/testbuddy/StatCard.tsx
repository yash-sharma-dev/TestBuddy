import { Card } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { TrendingUp, type LucideIcon } from "lucide-react";

interface StatCardProps {
  label: string;
  value: string | number;
  trend?: string;
  icon: LucideIcon;
  tone?: "primary" | "success" | "destructive" | "accent";
}

const toneMap = {
  primary: "bg-primary/10 text-primary",
  success: "bg-success/10 text-success",
  destructive: "bg-destructive/10 text-destructive",
  accent: "bg-accent/10 text-accent",
};

export function StatCard({ label, value, trend, icon: Icon, tone = "primary" }: StatCardProps) {
  return (
    <Card className="hover-lift group relative overflow-hidden border-border/60 p-5 shadow-card">
      <div className="flex items-start justify-between">
        <div className="space-y-1">
          <p className="text-sm font-medium text-muted-foreground">{label}</p>
          <p className="text-3xl font-bold tracking-tight">{value}</p>
        </div>
        <div className={cn("flex h-10 w-10 items-center justify-center rounded-xl transition-transform group-hover:scale-110", toneMap[tone])}>
          <Icon className="h-5 w-5" />
        </div>
      </div>
      {trend && (
        <div className="mt-3 flex items-center gap-1 text-xs text-muted-foreground">
          <TrendingUp className="h-3 w-3 text-success" />
          <span>{trend}</span>
        </div>
      )}
    </Card>
  );
}

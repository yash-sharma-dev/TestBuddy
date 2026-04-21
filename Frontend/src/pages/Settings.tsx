import { Bell, Moon, ShieldCheck, Sun } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Button } from "@/components/ui/button";
import { useTheme } from "@/components/theme/ThemeProvider";

export default function Settings() {
  const { theme, setTheme } = useTheme();

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h1 className="text-2xl font-bold tracking-tight">Settings</h1>
        <p className="text-sm text-muted-foreground">Configure your personal preferences.</p>
      </div>

      <Card className="shadow-card">
        <CardHeader>
          <CardTitle className="text-lg">Preferences</CardTitle>
          <CardDescription>These controls are ready for backend integration.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="rounded-lg border p-4">
            <p className="text-sm font-semibold">Theme</p>
            <p className="mt-1 text-xs text-muted-foreground">Choose your preferred color mode for the interface.</p>
            <div className="mt-3 flex gap-2">
              <Button
                type="button"
                variant={theme === "light" ? "default" : "outline"}
                onClick={() => setTheme("light")}
              >
                <Sun className="mr-2 h-4 w-4" />
                Light
              </Button>
              <Button
                type="button"
                variant={theme === "dark" ? "default" : "outline"}
                onClick={() => setTheme("dark")}
              >
                <Moon className="mr-2 h-4 w-4" />
                Dark
              </Button>
            </div>
          </div>

          <SettingRow
            id="email-alerts"
            icon={Bell}
            title="Email alerts"
            description="Get notified when long-running tests finish."
            defaultChecked
          />
          <SettingRow
            id="security-warnings"
            icon={ShieldCheck}
            title="Security warnings"
            description="Show warnings for potentially unsafe API targets."
            defaultChecked
          />
        </CardContent>
      </Card>
    </div>
  );
}

function SettingRow({
  id,
  icon: Icon,
  title,
  description,
  defaultChecked = false,
}: {
  id: string;
  icon: typeof Bell;
  title: string;
  description: string;
  defaultChecked?: boolean;
}) {
  return (
    <div className="flex items-center justify-between rounded-lg border p-4">
      <div className="flex min-w-0 items-start gap-3">
        <div className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-md bg-primary/10">
          <Icon className="h-4 w-4 text-primary" />
        </div>
        <div className="space-y-1">
          <Label htmlFor={id} className="text-sm font-semibold">
            {title}
          </Label>
          <p className="text-xs text-muted-foreground">{description}</p>
        </div>
      </div>
      <Switch id={id} defaultChecked={defaultChecked} />
    </div>
  );
}

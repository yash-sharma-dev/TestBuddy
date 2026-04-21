import { Mail, UserCircle2 } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useAppStore } from "@/store/appStore";
import { accountDisplayName } from "@/lib/utils";

export default function Profile() {
  const userEmail = useAppStore((s) => s.userEmail);
  const displayName = userEmail ? accountDisplayName(userEmail) : "Jane Developer";
  const emailLabel = userEmail || "jane@testbuddy.dev";

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h1 className="text-2xl font-bold tracking-tight">Profile</h1>
        <p className="text-sm text-muted-foreground">Manage your account information.</p>
      </div>

      <Card className="shadow-card">
        <CardHeader>
          <CardTitle className="text-lg">Account Details</CardTitle>
          <CardDescription>Basic information shown in your workspace header.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-5">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
              <UserCircle2 className="h-5 w-5 text-primary" />
            </div>
            <div>
              <p className="text-sm font-medium">{displayName}</p>
              <p className="text-xs text-muted-foreground">Display name</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
              <Mail className="h-5 w-5 text-primary" />
            </div>
            <div>
              <p className="text-sm font-medium">{emailLabel}</p>
              <p className="text-xs text-muted-foreground">Email address</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

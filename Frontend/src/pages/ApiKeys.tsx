import { useState, type FormEvent } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { KeyRound, Copy } from "lucide-react";
import { toast } from "sonner";

type ApiKeyRow = { id: string; name: string; preview: string; full: string; created: string };

function randomKeySegment() {
  return Math.random().toString(36).slice(2, 10);
}

export default function ApiKeys() {
  const [name, setName] = useState("");
  const [keys, setKeys] = useState<ApiKeyRow[]>([]);

  const handleGenerate = (e: FormEvent) => {
    e.preventDefault();
    const trimmed = name.trim();
    if (!trimmed) {
      toast.error("Enter a name for this key");
      return;
    }
    const full = `tb_live_${randomKeySegment()}${randomKeySegment()}`;
    const preview = `${full.slice(0, 12)}…`;
    const row: ApiKeyRow = {
      id: crypto.randomUUID(),
      name: trimmed,
      preview,
      full,
      created: new Date().toLocaleString(),
    };
    setKeys((prev) => [row, ...prev]);
    setName("");
    toast.success("API key created — copy it now; it won’t be shown again in full.");
  };

  const copyFull = async (full: string) => {
    try {
      await navigator.clipboard.writeText(full);
      toast.success("Copied to clipboard");
    } catch {
      toast.error("Could not copy — select and copy manually");
    }
  };

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h1 className="text-2xl font-bold tracking-tight">API Keys</h1>
        <p className="text-sm text-muted-foreground">Manage access tokens for your workspace integrations.</p>
      </div>

      <Card className="shadow-card">
        <form onSubmit={handleGenerate}>
          <CardHeader>
            <CardTitle className="text-lg">Create API Key</CardTitle>
            <CardDescription>Keys are generated locally for this demo. Plug in your backend when ready.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <label htmlFor="api-key-name" className="text-sm font-medium">
                Key name
              </label>
              <Input
                id="api-key-name"
                placeholder="e.g. CI runner key"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>

            <Button type="submit" className="gradient-primary">
              <KeyRound className="mr-2 h-4 w-4" />
              Generate key
            </Button>
          </CardContent>
        </form>
      </Card>

      {keys.length > 0 ? (
        <Card className="shadow-card">
          <CardHeader>
            <CardTitle className="text-lg">Your keys</CardTitle>
            <CardDescription>Preview only after creation — use copy for the full secret.</CardDescription>
          </CardHeader>
          <CardContent>
            <ul className="space-y-3">
              {keys.map((k) => (
                <li
                  key={k.id}
                  className="flex flex-col gap-2 rounded-lg border p-4 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div className="min-w-0 space-y-1">
                    <p className="text-sm font-medium">{k.name}</p>
                    <p className="font-mono text-xs text-muted-foreground">{k.preview}</p>
                    <p className="text-xs text-muted-foreground">Created {k.created}</p>
                  </div>
                  <Button type="button" variant="outline" size="sm" className="shrink-0" onClick={() => copyFull(k.full)}>
                    <Copy className="mr-2 h-4 w-4" />
                    Copy secret
                  </Button>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      ) : null}
    </div>
  );
}

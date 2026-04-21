import { useState, useRef, type DragEvent } from "react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { UploadCloud, FileJson, CheckCircle2, Globe, Sparkles, Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";

export default function UploadApi() {
  const [dragging, setDragging] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const [baseUrl, setBaseUrl] = useState("https://api.example.com");
  const [loading, setLoading] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();

  const handleDrop = (e: DragEvent) => {
    e.preventDefault();
    setDragging(false);
    const f = e.dataTransfer.files?.[0];
    if (f) setFile(f);
  };

  const handleGenerate = () => {
    if (!file) {
      toast.error("Please upload an OpenAPI/Swagger file first.");
      return;
    }
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      toast.success("Generated 24 test cases from your spec.");
      navigate("/test-cases");
    }, 1400);
  };

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="space-y-1">
        <h1 className="text-2xl font-bold tracking-tight">Upload API Specification</h1>
        <p className="text-sm text-muted-foreground">
          Drop your OpenAPI / Swagger file and let TestBuddy generate intelligent test cases automatically.
        </p>
      </div>

      <Card className="p-6 shadow-card md:p-8">
        <div
          onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
          onDragLeave={() => setDragging(false)}
          onDrop={handleDrop}
          onClick={() => inputRef.current?.click()}
          className={cn(
            "group relative flex cursor-pointer flex-col items-center justify-center rounded-2xl border-2 border-dashed p-10 text-center transition-all",
            dragging ? "border-primary bg-primary/5 scale-[1.01]" : "border-border hover:border-primary/50 hover:bg-muted/40",
          )}
        >
          <input
            ref={inputRef}
            type="file"
            accept=".json,.yaml,.yml"
            className="hidden"
            onChange={(e) => e.target.files?.[0] && setFile(e.target.files[0])}
          />
          {file ? (
            <div className="flex flex-col items-center gap-3 animate-scale-in">
              <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-success/10">
                <CheckCircle2 className="h-7 w-7 text-success" />
              </div>
              <div className="space-y-1">
                <p className="font-semibold">{file.name}</p>
                <p className="text-xs text-muted-foreground">{(file.size / 1024).toFixed(1)} KB · ready to process</p>
              </div>
              <button
                onClick={(e) => { e.stopPropagation(); setFile(null); }}
                className="text-xs font-medium text-primary hover:underline"
              >
                Choose a different file
              </button>
            </div>
          ) : (
            <>
              <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/10 transition-transform group-hover:scale-110">
                <UploadCloud className="h-7 w-7 text-primary" />
              </div>
              <p className="text-base font-semibold">Drop your OpenAPI file here</p>
              <p className="mt-1 text-sm text-muted-foreground">or click to browse — supports .json, .yaml, .yml</p>
            </>
          )}
        </div>

        <div className="mt-6 space-y-2">
          <Label htmlFor="baseUrl" className="text-sm font-medium">Base URL</Label>
          <div className="relative">
            <Globe className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              id="baseUrl"
              value={baseUrl}
              onChange={(e) => setBaseUrl(e.target.value)}
              className="pl-9"
              placeholder="https://api.example.com"
            />
          </div>
        </div>

        <Button
          size="lg"
          onClick={handleGenerate}
          disabled={loading}
          className="mt-6 w-full gradient-primary shadow-glow hover:opacity-95"
        >
          {loading ? (
            <><Loader2 className="h-4 w-4 animate-spin" /> Analyzing spec…</>
          ) : (
            <><Sparkles className="h-4 w-4" /> Generate Test Cases</>
          )}
        </Button>
      </Card>

      <Card className="p-5 shadow-card">
        <h3 className="mb-3 flex items-center gap-2 text-sm font-semibold">
          <FileJson className="h-4 w-4 text-primary" /> Helpful tips
        </h3>
        <ul className="space-y-2 text-sm text-muted-foreground">
          <li>• OpenAPI 3.0+ and Swagger 2.0 are both supported.</li>
          <li>• Make sure your spec includes example payloads for richer tests.</li>
          <li>• The base URL overrides the <code className="rounded bg-muted px-1 py-0.5 text-xs">servers</code> entry in your spec.</li>
          <li>• Authentication tokens can be configured after generation.</li>
        </ul>
      </Card>
    </div>
  );
}

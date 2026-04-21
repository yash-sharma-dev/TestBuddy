import { useState, useRef, type DragEvent } from "react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { UploadCloud, FileJson, CheckCircle2, Globe, Sparkles, Loader2, Lock, MessageSquareText } from "lucide-react";
import { cn } from "@/lib/utils";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";
import { useAppStore, type Environment, type AuthType } from "@/store/appStore";
import { uploadSpec, generateTests } from "@/lib/api";

export default function UploadApi() {
  const [dragging, setDragging] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();

  const {
    targetBaseUrl,
    setTargetBaseUrl,
    environment,
    setEnvironment,
    instructions,
    setInstructions,
    authType,
    setAuthType,
    authValue,
    setAuthValue,
    isGenerating,
    setGenerating,
    setRunId,
    setSpecContent,
    setParsedSpec,
    setTestCases,
  } = useAppStore();

  const handleDrop = (e: DragEvent) => {
    e.preventDefault();
    setDragging(false);
    const f = e.dataTransfer.files?.[0];
    if (f) handleFileSelected(f);
  };

  const handleFileSelected = (f: File) => {
    setFile(f);
    // Read file content immediately and store in appStore
    const reader = new FileReader();
    reader.onload = () => {
      if (typeof reader.result === "string") {
        setSpecContent(reader.result);
      }
    };
    reader.readAsText(f);
  };

  const handleGenerate = async () => {
    if (!file) {
      toast.error("Please upload an OpenAPI/Swagger file first.");
      return;
    }

    setGenerating(true);

    try {
      // Step 1: Upload the spec file
      const uploadResponse = await uploadSpec(file, environment);
      const parsed = uploadResponse.data;
      setRunId(parsed.runId);
      setParsedSpec(parsed);

      // Step 2: Generate test cases
      const specText = useAppStore.getState().specContent;
      if (!specText) {
        toast.error("Could not read spec file content.");
        setGenerating(false);
        return;
      }

      const generateResponse = await generateTests(specText, {
        runId: parsed.runId,
        instructions,
        targetBaseUrl,
        environment,
        authType,
        authValue,
      });

      setTestCases(generateResponse.data);
      toast.success(`Generated ${generateResponse.data.length} test cases`);
      navigate("/test-cases");
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to generate test cases";
      toast.error(message);
    } finally {
      setGenerating(false);
    }
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
            onChange={(e) => e.target.files?.[0] && handleFileSelected(e.target.files[0])}
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
                onClick={(e) => { e.stopPropagation(); setFile(null); setSpecContent(null); }}
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
              value={targetBaseUrl}
              onChange={(e) => setTargetBaseUrl(e.target.value)}
              className="pl-9"
              placeholder="https://api.example.com"
            />
          </div>
        </div>

        {/* Instructions textarea */}
        <div className="mt-4 space-y-2">
          <Label htmlFor="instructions" className="text-sm font-medium flex items-center gap-1.5">
            <MessageSquareText className="h-3.5 w-3.5" /> Instructions
          </Label>
          <Textarea
            id="instructions"
            value={instructions}
            onChange={(e) => setInstructions(e.target.value)}
            placeholder="e.g. Test creating a pet, test getting a pet by ID, test with missing required fields"
            rows={3}
          />
        </div>

        {/* Environment + Auth row */}
        <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div className="space-y-2">
            <Label className="text-sm font-medium">Environment</Label>
            <Select value={environment} onValueChange={(v) => setEnvironment(v as Environment)}>
              <SelectTrigger>
                <SelectValue placeholder="Select environment" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="dev">Development</SelectItem>
                <SelectItem value="staging">Staging</SelectItem>
                <SelectItem value="prod">Production</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label className="text-sm font-medium flex items-center gap-1.5">
              <Lock className="h-3.5 w-3.5" /> Authentication
            </Label>
            <Select value={authType} onValueChange={(v) => setAuthType(v as AuthType)}>
              <SelectTrigger>
                <SelectValue placeholder="Auth type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="NONE">None</SelectItem>
                <SelectItem value="JWT">JWT Token</SelectItem>
                <SelectItem value="API_KEY">API Key</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        {/* Auth token input — shown only when auth is needed */}
        {authType !== "NONE" && (
          <div className="mt-4 space-y-2 animate-fade-in">
            <Label htmlFor="authValue" className="text-sm font-medium">
              {authType === "JWT" ? "Bearer Token" : "API Key"}
            </Label>
            <Input
              id="authValue"
              type="password"
              value={authValue}
              onChange={(e) => setAuthValue(e.target.value)}
              placeholder={authType === "JWT" ? "eyJhbGciOiJIUzI1NiIs…" : "sk-live-xxxxxxxxxxxx"}
            />
          </div>
        )}

        <Button
          size="lg"
          onClick={handleGenerate}
          disabled={isGenerating}
          className="mt-6 w-full gradient-primary shadow-glow hover:opacity-95"
        >
          {isGenerating ? (
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

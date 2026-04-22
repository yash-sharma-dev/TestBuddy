import { useState, type FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import axios from "axios";

import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useAppStore } from "@/store/appStore";

export default function SignUp() {
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? "/";
  const setSession = useAppStore((s) => s.setSession);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (!email.trim() || !password.trim()) {
      toast.error("Please enter email and password");
      return;
    }

    try {
      const res = await axios.post("http://localhost:8080/api/auth/signup", {
        email,
        password,
      });

      if (res.data.success) {
        toast.success("Account created successfully");

        // auto login after signup
        localStorage.setItem("token", "dummy-token");
        setSession(email.trim());

        navigate(from, { replace: true });
      } else {
        toast.error(res.data.message || "Signup failed");
      }
    } catch (err) {
      toast.error("Server error");
    }
  };

  return (
    <div className="mx-auto max-w-md space-y-6">
      <div className="space-y-1 text-center">
        <h1 className="text-2xl font-bold tracking-tight">
          Create an account
        </h1>
        <p className="text-sm text-muted-foreground">
          Sign up to save runs and sync across devices.
        </p>
      </div>

      <Card className="shadow-card">
        <form onSubmit={handleSubmit}>
          <CardHeader>
            <CardTitle className="text-lg">Sign up</CardTitle>
            <CardDescription>
              Now connected to backend
            </CardDescription>
          </CardHeader>

          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="signup-email">Email</Label>
              <Input
                id="signup-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="signup-password">Password</Label>
              <Input
                id="signup-password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </CardContent>

          <CardFooter className="flex flex-col gap-4">
            <Button type="submit" className="w-full gradient-primary">
              Sign up
            </Button>

            <p className="text-center text-sm text-muted-foreground">
              Already have an account?{" "}
              <Link
                to="/signin"
                className="font-medium text-primary hover:underline"
              >
                Sign in
              </Link>
            </p>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
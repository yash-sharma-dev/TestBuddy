import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { AppLayout } from "@/components/layout/AppLayout";
import Dashboard from "./pages/Dashboard";
import UploadApi from "./pages/UploadApi";
import TestCases from "./pages/TestCases";
import Results from "./pages/Results";
import Insights from "./pages/Insights";
import Usage from "./pages/Usage";
import NotFound from "./pages/NotFound.tsx";
import Profile from "./pages/Profile";
import Settings from "./pages/Settings";
import ApiKeys from "./pages/ApiKeys";
import Notifications from "./pages/Notifications";
import SignUp from "./pages/SignUp";
import SignIn from "./pages/SignIn";
import { ThemeProvider } from "@/components/theme/ThemeProvider";
import { ProtectedOutlet } from "@/components/layout/ProtectedOutlet";

const queryClient = new QueryClient();

const App = () => (
  <ThemeProvider>
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>
        <Toaster />
        <Sonner />
        <BrowserRouter>
          <Routes>
            <Route element={<AppLayout />}>
              <Route path="/" element={<Dashboard />} />
              <Route path="/upload" element={<UploadApi />} />
              <Route path="/test-cases" element={<TestCases />} />
              <Route path="/results" element={<Results />} />
              <Route path="/insights" element={<Insights />} />
              <Route path="/usage" element={<Usage />} />
              <Route element={<ProtectedOutlet />}>
                <Route path="/profile" element={<Profile />} />
                <Route path="/settings" element={<Settings />} />
                <Route path="/api-keys" element={<ApiKeys />} />
                <Route path="/notifications" element={<Notifications />} />
              </Route>
              <Route path="/signup" element={<SignUp />} />
              <Route path="/signin" element={<SignIn />} />
            </Route>
            {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </TooltipProvider>
    </QueryClientProvider>
  </ThemeProvider>
);

export default App;

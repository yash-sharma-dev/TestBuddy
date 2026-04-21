import { Bell, Moon, Search, Sun } from "lucide-react";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { useAppStore } from "@/store/appStore";
import { useTheme } from "@/components/theme/ThemeProvider";
import { accountDisplayName, initialsFromEmail } from "@/lib/utils";

export function AppHeader() {
  const navigate = useNavigate();
  const resetStore = useAppStore((state) => state.reset);
  const isAuthenticated = useAppStore((state) => state.isAuthenticated);
  const userEmail = useAppStore((state) => state.userEmail);
  const { theme, setTheme } = useTheme();
  const isDarkMode = theme === "dark";

  const handleSignOut = () => {
    resetStore();
    toast.success("Signed out successfully");
    navigate("/");
  };

  return (
    <header className="sticky top-0 z-30 flex h-16 items-center gap-3 border-b bg-background/80 px-4 backdrop-blur-md md:px-6">
      <SidebarTrigger className="text-muted-foreground hover:text-foreground" />

      <div className="relative ml-2 hidden max-w-md flex-1 md:block">
        <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          placeholder="Search tests, endpoints, results…"
          className="h-9 border-transparent bg-muted pl-9 focus-visible:bg-background"
        />
      </div>

      <div className="ml-auto flex flex-shrink-0 items-center gap-2">
        {isAuthenticated ? (
          <Button variant="ghost" size="icon" className="relative" asChild>
            <Link to="/notifications" aria-label="Notifications">
              <Bell className="h-4 w-4" />
              <span className="absolute right-2 top-2 h-2 w-2 animate-pulse-soft rounded-full bg-destructive" />
            </Link>
          </Button>
        ) : null}

        {!isAuthenticated ? (
          <>
            <Button variant="ghost" size="sm" asChild>
              <Link to="/signin">Sign in</Link>
            </Button>
            <Button size="sm" className="gradient-primary" asChild>
              <Link to="/signup">Sign up</Link>
            </Button>
          </>
        ) : null}

        <Button
          variant="ghost"
          size="icon"
          onClick={() => setTheme(isDarkMode ? "light" : "dark")}
          aria-label={isDarkMode ? "Switch to light mode" : "Switch to dark mode"}
          title={isDarkMode ? "Switch to light mode" : "Switch to dark mode"}
        >
          {isDarkMode ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
        </Button>

        {isAuthenticated ? (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button className="flex items-center gap-2 rounded-full p-1 transition-colors hover:bg-muted">
                <Avatar className="h-8 w-8">
                  <AvatarFallback className="gradient-primary text-xs font-semibold text-primary-foreground">
                    {userEmail ? initialsFromEmail(userEmail) : "JD"}
                  </AvatarFallback>
                </Avatar>
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <DropdownMenuLabel>
                <div className="flex flex-col">
                  <span className="text-sm font-medium">
                    {userEmail ? accountDisplayName(userEmail) : "Jane Developer"}
                  </span>
                  <span className="text-xs text-muted-foreground">
                    {userEmail || "jane@testbuddy.dev"}
                  </span>
                </div>
              </DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem asChild>
                <Link to="/profile">Profile</Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild>
                <Link to="/settings">Settings</Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild>
                <Link to="/api-keys">API Keys</Link>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={handleSignOut}>Sign out</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ) : null}
      </div>
    </header>
  );
}

import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAppStore } from "@/store/appStore";

export function ProtectedOutlet() {
  const isAuthenticated = useAppStore((s) => s.isAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/signin" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}

import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function accountDisplayName(email: string): string {
  const lower = email.trim().toLowerCase();
  if (lower === "jane@testbuddy.dev") return "Jane Developer";
  return displayNameFromEmail(email);
}

export function initialsFromEmail(email: string): string {
  const lower = email.trim().toLowerCase();
  if (lower === "jane@testbuddy.dev") return "JD";
  const part = email.split("@")[0]?.trim() ?? "";
  if (!part) return "?";
  const bits = part.split(/[._-]+/).filter(Boolean);
  if (bits.length >= 2) return (bits[0][0] + bits[1][0]).toUpperCase();
  return part.slice(0, 2).toUpperCase();
}

export function displayNameFromEmail(email: string): string {
  const local = email.split("@")[0]?.trim() ?? "";
  if (!local) return "Account";
  return local
    .split(/[._-]+/)
    .map((s) => s.charAt(0).toUpperCase() + s.slice(1).toLowerCase())
    .join(" ");
}

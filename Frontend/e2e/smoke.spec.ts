import { test, expect } from "@playwright/test";

test("sign-in page loads and shows form", async ({ page }) => {
  await page.goto("/signin");

  await expect(page.getByRole("heading", { name: /sign in/i })).toBeVisible();
  await expect(page.getByLabel(/email/i)).toBeVisible();
  await expect(page.getByLabel(/password/i)).toBeVisible();
  await expect(page.getByRole("button", { name: /sign in/i })).toBeVisible();
});

test("invalid credentials show an error", async ({ page }) => {
  await page.goto("/signin");

  await page.getByLabel(/email/i).fill("nobody@example.com");
  await page.getByLabel(/password/i).fill("wrongpassword");
  await page.getByRole("button", { name: /sign in/i }).click();

  await expect(page.getByRole("alert")).toBeVisible({ timeout: 5000 });
});

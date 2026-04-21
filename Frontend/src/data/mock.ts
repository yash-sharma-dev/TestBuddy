export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH";
export type TestStatus = "passed" | "failed" | "not_run";

export interface TestCase {
  id: string;
  name: string;
  endpoint: string;
  method: HttpMethod;
  payload: Record<string, unknown> | null;
  expectedStatus: number;
  status: TestStatus;
  responseCode?: number;
  responseTime?: number;
  errorMessage?: string;
}

export const stats = {
  totalApis: 24,
  totalTests: 186,
  passedTests: 142,
  failedTests: 44,
  trends: {
    apis: "+3 this week",
    tests: "+28 this week",
    passed: "+19%",
    failed: "-4%",
  },
};

export const recentActivity = [
  { id: "1", name: "Create user account", endpoint: "/api/v1/users", method: "POST" as HttpMethod, status: "passed" as TestStatus, time: "2 min ago", duration: "142ms" },
  { id: "2", name: "Fetch order details", endpoint: "/api/v1/orders/:id", method: "GET" as HttpMethod, status: "failed" as TestStatus, time: "8 min ago", duration: "1.2s" },
  { id: "3", name: "Update product inventory", endpoint: "/api/v1/products/:id", method: "PUT" as HttpMethod, status: "passed" as TestStatus, time: "15 min ago", duration: "98ms" },
  { id: "4", name: "Delete cart item", endpoint: "/api/v1/cart/:id", method: "DELETE" as HttpMethod, status: "passed" as TestStatus, time: "32 min ago", duration: "67ms" },
  { id: "5", name: "Authenticate user", endpoint: "/api/v1/auth/login", method: "POST" as HttpMethod, status: "failed" as TestStatus, time: "1 hr ago", duration: "890ms" },
  { id: "6", name: "List all products", endpoint: "/api/v1/products", method: "GET" as HttpMethod, status: "passed" as TestStatus, time: "2 hr ago", duration: "215ms" },
];

export const trendData = [
  { day: "Mon", runs: 24, passed: 20 },
  { day: "Tue", runs: 32, passed: 28 },
  { day: "Wed", runs: 28, passed: 22 },
  { day: "Thu", runs: 45, passed: 38 },
  { day: "Fri", runs: 38, passed: 30 },
  { day: "Sat", runs: 18, passed: 16 },
  { day: "Sun", runs: 22, passed: 19 },
];

export const testCases: TestCase[] = [
  {
    id: "tc1",
    name: "Create new user with valid payload",
    endpoint: "/api/v1/users",
    method: "POST",
    payload: { name: "Jane Doe", email: "jane@example.com", role: "user" },
    expectedStatus: 201,
    status: "passed",
    responseCode: 201,
    responseTime: 142,
  },
  {
    id: "tc2",
    name: "Fetch user by ID",
    endpoint: "/api/v1/users/42",
    method: "GET",
    payload: null,
    expectedStatus: 200,
    status: "passed",
    responseCode: 200,
    responseTime: 89,
  },
  {
    id: "tc3",
    name: "Update product price",
    endpoint: "/api/v1/products/15",
    method: "PUT",
    payload: { price: 29.99, currency: "USD" },
    expectedStatus: 200,
    status: "failed",
    responseCode: 422,
    responseTime: 312,
    errorMessage: "Validation failed: price must be a positive integer in cents",
  },
  {
    id: "tc4",
    name: "Delete order",
    endpoint: "/api/v1/orders/9981",
    method: "DELETE",
    payload: null,
    expectedStatus: 204,
    status: "passed",
    responseCode: 204,
    responseTime: 67,
  },
  {
    id: "tc5",
    name: "Authenticate with invalid credentials",
    endpoint: "/api/v1/auth/login",
    method: "POST",
    payload: { email: "test@example.com", password: "wrong" },
    expectedStatus: 401,
    status: "failed",
    responseCode: 500,
    responseTime: 890,
    errorMessage: "Internal server error: NullPointerException at AuthService.validate(line 88)",
  },
  {
    id: "tc6",
    name: "List products with pagination",
    endpoint: "/api/v1/products?page=1&limit=20",
    method: "GET",
    payload: null,
    expectedStatus: 200,
    status: "passed",
    responseCode: 200,
    responseTime: 215,
  },
  {
    id: "tc7",
    name: "Patch user profile",
    endpoint: "/api/v1/users/42",
    method: "PATCH",
    payload: { bio: "Senior engineer" },
    expectedStatus: 200,
    status: "failed",
    responseCode: 404,
    responseTime: 76,
    errorMessage: "Resource not found: user with id 42 does not exist in test environment",
  },
  {
    id: "tc8",
    name: "Add item to cart",
    endpoint: "/api/v1/cart/items",
    method: "POST",
    payload: { productId: 15, quantity: 2 },
    expectedStatus: 201,
    status: "passed",
    responseCode: 201,
    responseTime: 134,
  },
];

export const performanceData = [
  { endpoint: "/users", time: 142 },
  { endpoint: "/orders", time: 286 },
  { endpoint: "/products", time: 215 },
  { endpoint: "/auth", time: 890 },
  { endpoint: "/cart", time: 134 },
  { endpoint: "/payments", time: 412 },
];

export const aiInsights: Record<string, { rootCause: string; suggestion: string; snippet: string }> = {
  tc3: {
    rootCause: "The API expects price in cents (integer) but the test payload sends a float in dollars. The validation layer rejects non-integer prices, returning a 422 Unprocessable Entity.",
    suggestion: "Convert the price to cents before sending. Multiply by 100 and round to the nearest integer to match the schema contract.",
    snippet: `// Before\npayload: { price: 29.99, currency: "USD" }\n\n// After\npayload: { price: Math.round(29.99 * 100), currency: "USD" }`,
  },
  tc5: {
    rootCause: "The auth service throws a NullPointerException when the email is not found in the user table. The handler should return 401 Unauthorized but instead leaks a 500 error.",
    suggestion: "Add a null check in AuthService.validate() before accessing user properties. Return 401 for any invalid credential combination.",
    snippet: `// AuthService.java\npublic AuthResult validate(String email, String password) {\n  User user = userRepo.findByEmail(email);\n  if (user == null) return AuthResult.unauthorized(); // add this\n  return user.checkPassword(password)\n    ? AuthResult.ok(user)\n    : AuthResult.unauthorized();\n}`,
  },
  tc7: {
    rootCause: "The test references user ID 42 which does not exist in the seeded test database. The PATCH endpoint correctly returns 404, but the test expected the user to be present.",
    suggestion: "Either seed the test fixture with user ID 42 in the setup hook, or update the test to first create a user and use the returned ID.",
    snippet: `// In test setup\nbeforeAll(async () => {\n  const user = await api.post("/users", { name: "Test", email: "t@x.com" });\n  testUserId = user.data.id;\n});`,
  },
};

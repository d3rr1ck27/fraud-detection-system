// Thin wrappers around the Spring Boot backend's transaction endpoints.
// Centralising fetch calls here keeps components free of hard-coded URLs
// and makes it easy to swap in axios, add auth headers, or change the
// base URL in one place.

const BASE_URL = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';

/**
 * POST a new transaction to the backend.
 * The backend will call the Python ML service, persist the result,
 * and return the saved Transaction entity (with id, fraud, confidence,
 * riskLevel, and createdAt populated).
 *
 * @param {{ time: number, amount: number, features: number[] }} data
 * @returns {Promise<object>} the saved transaction
 */
export async function postTransaction(data) {
  const response = await fetch(`${BASE_URL}/api/transactions`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    // Surface backend error text so the UI can show something useful.
    const text = await response.text();
    throw new Error(`POST /api/transactions failed (${response.status}): ${text}`);
  }
  return response.json();
}

/**
 * GET every transaction the backend has stored.
 * Used to populate the history table after each new prediction.
 *
 * @returns {Promise<object[]>} list of saved transactions
 */
export async function getTransactions() {
  const response = await fetch(`${BASE_URL}/api/transactions`);

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`GET /api/transactions failed (${response.status}): ${text}`);
  }
  return response.json();
}

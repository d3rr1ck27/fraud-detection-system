import React, { useEffect, useState, useCallback } from "react";
import TransactionForm from "./components/TransactionForm";
import ResultCard from "./components/ResultCard";
import TransactionTable from "./components/TransactionTable";
import { postTransaction, getTransactions } from "./services/api";
import "./App.css";

/**
 * Top-level dashboard.
 *
 * Layout (top → bottom):
 *   1. Transaction form — operator submits a transaction to score.
 *   2. Result card     — shows the most recent prediction (if any).
 *   3. Transaction table — full history pulled from the backend.
 *
 * State lives here so the form, result card, and table stay in sync
 * after each submission.
 */
function App() {
  // Last prediction returned by the backend. Null until the first submit.
  const [lastResult, setLastResult] = useState(null);

  // Full transaction history rendered in the table.
  const [transactions, setTransactions] = useState([]);

  // Top-level error banner (e.g. backend unreachable).
  const [error, setError] = useState(null);

  // Wrapped in useCallback so the effect dependency array is stable.
  const refreshTransactions = useCallback(async () => {
    try {
      const list = await getTransactions();
      setTransactions(list);
    } catch (err) {
      setError(err.message || "Could not load transactions");
    }
  }, []);

  // Initial load on mount.
  useEffect(() => {
    refreshTransactions();
  }, [refreshTransactions]);

  // Called by TransactionForm with the assembled payload.
  const handleSubmit = async (payload) => {
    setError(null);
    const saved = await postTransaction(payload);
    setLastResult(saved);
    // Refresh the table so the newly-saved row appears alongside history.
    await refreshTransactions();
  };

  return (
    <div className="app-shell">
      <header className="app-header">
        <h1>Fraud Detection Dashboard</h1>
      </header>

      {error && <div className="app-error">{error}</div>}

      <section className="app-section">
        <TransactionForm onSubmit={handleSubmit} />
        <ResultCard transaction={lastResult} />
      </section>

      <section className="app-section">
        <h2>Transaction history</h2>
        <TransactionTable transactions={transactions} />
      </section>
    </div>
  );
}

export default App;

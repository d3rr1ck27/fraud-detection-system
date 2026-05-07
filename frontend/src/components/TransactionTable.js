import React from "react";

/**
 * Read-only table listing every persisted transaction.
 *
 * Props:
 *   - transactions: array of Transaction objects from the backend.
 *     May be empty (e.g. before the first submission).
 */
function TransactionTable({ transactions }) {
  if (!transactions || transactions.length === 0) {
    return (
      <div className="empty-state">
        No transactions yet. Submit one above to get started.
      </div>
    );
  }

  // Format the LocalDateTime string returned by Spring (ISO-8601) into
  // something a human wants to look at. We use the user's locale so the
  // formatting matches their browser's preferences.
  const formatDate = (raw) => {
    if (!raw) return "";
    const d = new Date(raw);
    return Number.isNaN(d.getTime()) ? raw : d.toLocaleString();
  };

  return (
    <table className="tx-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Amount</th>
          <th>Time</th>
          <th>Fraud</th>
          <th>Confidence</th>
          <th>Risk Level</th>
          <th>Created At</th>
        </tr>
      </thead>
      <tbody>
        {transactions.map((tx) => (
          // Highlight fraudulent rows so they pop visually in a long table.
          <tr key={tx.id} className={tx.fraud ? "tx-row-fraud" : ""}>
            <td>{tx.id}</td>
            <td>${Number(tx.amount).toFixed(2)}</td>
            <td>{tx.time}</td>
            <td>{tx.fraud ? "Yes" : "No"}</td>
            <td>{(tx.confidence * 100).toFixed(2)}%</td>
            <td>{tx.riskLevel}</td>
            <td>{formatDate(tx.createdAt)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export default TransactionTable;

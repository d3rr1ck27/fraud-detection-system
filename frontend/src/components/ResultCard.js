import React from "react";

/**
 * Displays the most recent prediction returned by the backend.
 *
 * Props:
 *   - transaction: the saved Transaction object from the backend
 *     ({ id, amount, time, fraud, confidence, riskLevel, ... }) or
 *     null if no prediction has been made yet.
 */
function ResultCard({ transaction }) {
  // Render nothing until we have a result — the parent decides
  // when to show this component.
  if (!transaction) return null;

  const { id, amount, fraud, confidence, riskLevel } = transaction;

  // Top-level styling: red tint for fraud, green tint for legitimate.
  const containerClass = `result-card ${fraud ? "result-fraud" : "result-legit"}`;

  // Risk badge colour driven by the bucket the backend assigned.
  // Falling back to grey keeps the UI sane if the backend ever returns
  // an unexpected value.
  const riskClassMap = {
    LOW: "risk-low",
    MEDIUM: "risk-medium",
    HIGH: "risk-high",
  };
  const riskClass = riskClassMap[riskLevel] || "risk-unknown";

  // Confidence comes from the model as a 0..1 probability of fraud.
  const confidencePct = (confidence * 100).toFixed(2);

  return (
    <div className={containerClass}>
      <div className="result-headline">{fraud ? "FRAUD" : "LEGITIMATE"}</div>
      <div className="result-meta">
        <span>Confidence: {confidencePct}%</span>
        <span className={`risk-badge ${riskClass}`}>{riskLevel}</span>
      </div>
      <div className="result-tx">
        Transaction #{id} &middot; ${Number(amount).toFixed(2)}
      </div>
    </div>
  );
}

export default ResultCard;

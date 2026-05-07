import React, { useState } from "react";

// Number of PCA-transformed feature columns the model expects.
// The Kaggle creditcard dataset has V1..V28, which we mirror here.
const FEATURE_COUNT = 28;

/**
 * Form for submitting a single transaction to the backend.
 *
 * The advanced V1..V28 inputs default to a hidden state so casual users
 * can submit a transaction with just amount and time (the V-features
 * default to 0, which is roughly the mean of each PCA component).
 *
 * Props:
 *   - onSubmit(payload): called with { time, amount, features } when
 *     the form is submitted. The parent owns the actual API call.
 */
function TransactionForm({ onSubmit }) {
  // Top-level inputs. Stored as strings so empty fields don't get
  // forced to 0; we coerce to Number on submit.
  const [amount, setAmount] = useState("");
  const [time, setTime] = useState("");

  // V1..V28. Initialised to zeros so the form is submittable without
  // expanding the advanced section.
  const [features, setFeatures] = useState(() => Array(FEATURE_COUNT).fill("0"));

  // Whether the V1..V28 panel is visible.
  const [showAdvanced, setShowAdvanced] = useState(false);

  // Submission state — disable the button + show feedback while in flight.
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  // Update one feature index without mutating state.
  const handleFeatureChange = (index, value) => {
    setFeatures((prev) => {
      const next = [...prev];
      next[index] = value;
      return next;
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      // Coerce strings to numbers right before sending so the backend
      // sees a clean numeric payload regardless of how the inputs render.
      const payload = {
        time: Number(time),
        amount: Number(amount),
        features: features.map((v) => Number(v)),
      };
      await onSubmit(payload);
    } catch (err) {
      setError(err.message || "Submission failed");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="transaction-form" onSubmit={handleSubmit}>
      <h2>Score a transaction</h2>

      {/* Required basic inputs */}
      <div className="form-row">
        <label>
          Amount
          <input
            type="number"
            step="any"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
          />
        </label>
        <label>
          Time
          <input
            type="number"
            step="any"
            value={time}
            onChange={(e) => setTime(e.target.value)}
            required
          />
        </label>
      </div>

      {/* Toggle for the V1..V28 panel */}
      <button
        type="button"
        className="toggle-advanced"
        onClick={() => setShowAdvanced((v) => !v)}
      >
        {showAdvanced ? "Hide advanced features" : "Show advanced features"}
      </button>

      {showAdvanced && (
        <div className="features-grid">
          {features.map((value, idx) => (
            <label key={idx} className="feature-input">
              V{idx + 1}
              <input
                type="number"
                step="any"
                value={value}
                onChange={(e) => handleFeatureChange(idx, e.target.value)}
              />
            </label>
          ))}
        </div>
      )}

      {error && <div className="form-error">{error}</div>}

      <button type="submit" className="submit-btn" disabled={submitting}>
        {submitting ? "Scoring…" : "Submit"}
      </button>
    </form>
  );
}

export default TransactionForm;

"""FastAPI prediction service for the fraud detection model.

Loads the trained RandomForest classifier and the StandardScaler that was
fit during training, then exposes:

    GET  /health   — liveness check + model load status
    POST /predict  — score a single transaction
"""

from contextlib import asynccontextmanager
from pathlib import Path
from typing import List

import joblib
import numpy as np
import pandas as pd
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field


# ---------------------------------------------------------------------------
# Paths — resolved relative to THIS file so the service works regardless of
# the current working directory (important when running under Docker, gunicorn,
# or `uvicorn ml_service.main:app` from a different folder).
# ---------------------------------------------------------------------------
BASE_DIR = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "model" / "fraud_model.pkl"
SCALER_PATH = BASE_DIR / "model" / "scaler.pkl"

# Risk thresholds applied to the predicted fraud probability.
LOW_THRESHOLD = 0.4
HIGH_THRESHOLD = 0.7

# The training pipeline fit the scaler on the two-column frame
# X[["Amount", "Time"]] (Amount first, Time second), and the full feature
# matrix used Time, V1..V28, Amount in that order. We mirror both here.
SCALER_COLUMNS = ["Amount", "Time"]
FEATURE_COLUMNS = ["Time"] + [f"V{i}" for i in range(1, 29)] + ["Amount"]


# ---------------------------------------------------------------------------
# Pydantic request / response models
# ---------------------------------------------------------------------------
class PredictRequest(BaseModel):
    """Single transaction to score."""

    time: float = Field(..., description="Seconds elapsed since the first transaction in the dataset.")
    amount: float = Field(..., description="Transaction amount.")
    features: List[float] = Field(
        ...,
        min_length=28,
        max_length=28,
        description="The 28 PCA-transformed features V1..V28 in order.",
    )


class PredictResponse(BaseModel):
    fraud: bool
    confidence: float
    risk_level: str


# ---------------------------------------------------------------------------
# Application state — populated in the lifespan handler at startup.
# ---------------------------------------------------------------------------
state: dict = {
    "model": None,
    "scaler": None,
}


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load model and scaler once when the app starts."""
    try:
        state["model"] = joblib.load(MODEL_PATH)
        state["scaler"] = joblib.load(SCALER_PATH)
        print(f"Loaded model from  {MODEL_PATH}")
        print(f"Loaded scaler from {SCALER_PATH}")
    except FileNotFoundError as exc:
        # Don't crash the process — /health will report model_loaded=false
        # so the caller (or k8s) can react.
        print(f"WARNING: could not load model artifacts: {exc}")
    yield
    # No teardown needed.


app = FastAPI(title="Fraud Detection ML Service", lifespan=lifespan)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
def _risk_level(probability: float) -> str:
    """Bucket a fraud probability into LOW / MEDIUM / HIGH."""
    if probability < LOW_THRESHOLD:
        return "LOW"
    if probability < HIGH_THRESHOLD:
        return "MEDIUM"
    return "HIGH"


# ---------------------------------------------------------------------------
# Endpoints
# ---------------------------------------------------------------------------
@app.get("/health")
def health() -> dict:
    """Liveness probe. Reports whether the model artifacts loaded cleanly."""
    model_loaded = state["model"] is not None and state["scaler"] is not None
    return {"status": "ok", "model_loaded": model_loaded}


@app.post("/predict", response_model=PredictResponse)
def predict(req: PredictRequest) -> PredictResponse:
    """Score a single transaction and return fraud / confidence / risk level."""
    model = state["model"]
    scaler = state["scaler"]
    if model is None or scaler is None:
        # 503: the service is up but the model isn't ready.
        raise HTTPException(status_code=503, detail="Model not loaded")

    # 1. Scale the two raw numeric features (Amount, Time) using the same
    #    scaler that was fit during training. We wrap the values in a
    #    DataFrame with named columns so scikit-learn doesn't complain about
    #    feature-name mismatches.
    raw = pd.DataFrame([[req.amount, req.time]], columns=SCALER_COLUMNS)
    scaled = scaler.transform(raw)[0]
    scaled_amount, scaled_time = float(scaled[0]), float(scaled[1])

    # 2. Reassemble the full feature vector in the exact column order the
    #    model was trained on: Time, V1..V28, Amount.
    row = [scaled_time, *req.features, scaled_amount]
    X = pd.DataFrame([row], columns=FEATURE_COLUMNS)

    # 3. Predict. predict_proba()[:, 1] gives the probability of class 1 (fraud).
    proba = float(model.predict_proba(X)[0, 1])
    is_fraud = bool(proba >= 0.5)

    return PredictResponse(
        fraud=is_fraud,
        confidence=proba,
        risk_level=_risk_level(proba),
    )

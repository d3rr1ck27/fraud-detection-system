"""Train a fraud-detection model on the credit-card transactions dataset.

Pipeline:
    1. Load CSV with pandas
    2. Split into features X and target y
    3. Scale 'Amount' and 'Time' with StandardScaler
    4. Stratified 80/20 train/test split
    5. SMOTE on training set only (to address ~0.17% fraud rate)
    6. Fit a RandomForestClassifier
    7. Evaluate: confusion matrix, classification report, ROC-AUC
    8. Persist model and scaler with joblib
"""

from pathlib import Path

import joblib
import pandas as pd
from imblearn.over_sampling import SMOTE
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import (
    classification_report,
    confusion_matrix,
    roc_auc_score,
)
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler


# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------
BASE_DIR = Path(__file__).parent
DATA_DIR = BASE_DIR / "data"
FULL_DATA_PATH = DATA_DIR / "creditcard.csv"
SAMPLE_DATA_PATH = DATA_DIR / "creditcard_sample.csv"
MODEL_DIR = BASE_DIR / "model"
MODEL_PATH = MODEL_DIR / "fraud_model.pkl"
SCALER_PATH = MODEL_DIR / "scaler.pkl"

RANDOM_STATE = 42


def resolve_dataset_path() -> Path:
    """Resolve which dataset CSV to load.

    Preference order:
      1. data/creditcard.csv         (full dataset, used locally)
      2. data/creditcard_sample.csv  (smaller sample, shipped for deployment)

    Raises:
        FileNotFoundError: if neither file is present.
    """
    if FULL_DATA_PATH.exists():
        print(f"Using FULL dataset: {FULL_DATA_PATH}")
        return FULL_DATA_PATH

    if SAMPLE_DATA_PATH.exists():
        print(f"Using SAMPLE dataset: {SAMPLE_DATA_PATH}")
        return SAMPLE_DATA_PATH

    raise FileNotFoundError(
        "No dataset found. Expected one of:\n"
        f"  - {FULL_DATA_PATH}\n"
        f"  - {SAMPLE_DATA_PATH}\n"
        "Place a credit-card transactions CSV at one of those paths "
        "before running training."
    )


def train() -> None:
    # -----------------------------------------------------------------------
    # 1. Load the dataset
    #    Prefer the full creditcard.csv when present; otherwise fall back to
    #    the smaller creditcard_sample.csv that ships with the deployment.
    # -----------------------------------------------------------------------
    data_path = resolve_dataset_path()
    print(f"Loading dataset from {data_path} ...")
    df = pd.read_csv(data_path)
    print(f"  Loaded {df.shape[0]:,} rows, {df.shape[1]} columns.")

    # -----------------------------------------------------------------------
    # 2. Separate features (X) from target (y)
    #    The 'Class' column is the binary fraud label (1 = fraud).
    # -----------------------------------------------------------------------
    X = df.drop(columns=["Class"])
    y = df["Class"]

    # -----------------------------------------------------------------------
    # 3. Scale 'Amount' and 'Time'.
    #    The V1..V28 columns are already PCA-transformed and roughly centered,
    #    so only 'Amount' and 'Time' need standardization. We fit the scaler
    #    on the full feature frame here for simplicity, then save it so the
    #    inference service can apply the exact same transformation later.
    # -----------------------------------------------------------------------
    scaler = StandardScaler()
    X[["Amount", "Time"]] = scaler.fit_transform(X[["Amount", "Time"]])

    # -----------------------------------------------------------------------
    # 4. Stratified 80/20 train/test split.
    #    Stratifying on y preserves the (very imbalanced) class ratio in both
    #    splits so the test-set metrics remain meaningful.
    # -----------------------------------------------------------------------
    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.20,
        random_state=RANDOM_STATE,
        stratify=y,
    )
    print(
        f"  Train set: {X_train.shape[0]:,} rows  "
        f"(fraud={int(y_train.sum())})"
    )
    print(
        f"  Test set:  {X_test.shape[0]:,} rows  "
        f"(fraud={int(y_test.sum())})"
    )

    # -----------------------------------------------------------------------
    # 5. SMOTE on training data only.
    #    SMOTE synthesizes new minority-class (fraud) samples so the
    #    classifier sees a balanced training distribution. CRITICAL: never
    #    apply SMOTE to the test set, or evaluation metrics become invalid.
    # -----------------------------------------------------------------------
    print("Applying SMOTE to the training set ...")
    smote = SMOTE(random_state=RANDOM_STATE)
    X_train_res, y_train_res = smote.fit_resample(X_train, y_train)
    print(
        f"  After SMOTE: {X_train_res.shape[0]:,} rows  "
        f"(fraud={int(y_train_res.sum())}, "
        f"non-fraud={int((y_train_res == 0).sum())})"
    )

    # -----------------------------------------------------------------------
    # 6. Train a RandomForestClassifier.
    #    n_estimators=100 is a sensible default; n_jobs=-1 uses all cores.
    # -----------------------------------------------------------------------
    print("Training RandomForestClassifier ...")
    model = RandomForestClassifier(
        n_estimators=100,
        random_state=RANDOM_STATE,
        n_jobs=-1,
    )
    model.fit(X_train_res, y_train_res)

    # -----------------------------------------------------------------------
    # 7. Evaluate on the held-out test set.
    #    We report:
    #      - Confusion matrix (raw counts of TN / FP / FN / TP)
    #      - Classification report (precision, recall, f1 per class)
    #      - ROC-AUC (threshold-independent ranking quality)
    # -----------------------------------------------------------------------
    y_pred = model.predict(X_test)
    y_proba = model.predict_proba(X_test)[:, 1]

    print("\n=== Evaluation on test set ===")
    print("\nConfusion Matrix:")
    print(confusion_matrix(y_test, y_pred))

    print("\nClassification Report:")
    print(classification_report(y_test, y_pred, digits=4))

    roc_auc = roc_auc_score(y_test, y_proba)
    print(f"ROC-AUC Score: {roc_auc:.6f}")

    # -----------------------------------------------------------------------
    # 8. & 9. Persist the trained model and the fitted scaler.
    #         The scaler MUST be saved alongside the model so inference
    #         applies the same Amount/Time transformation.
    # -----------------------------------------------------------------------
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    joblib.dump(model, MODEL_PATH)
    joblib.dump(scaler, SCALER_PATH)
    print(f"\nSaved model to  {MODEL_PATH}")
    print(f"Saved scaler to {SCALER_PATH}")


if __name__ == "__main__":
    train()

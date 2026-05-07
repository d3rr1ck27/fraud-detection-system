"""Exploratory data analysis for the credit card fraud dataset.

Loads ``data/creditcard.csv`` and prints a quick overview: shape, columns,
class distribution, and whether any missing values are present.
"""

from pathlib import Path

import pandas as pd


DATA_PATH = Path(__file__).parent / "data" / "creditcard.csv"


def main() -> None:
    df = pd.read_csv(DATA_PATH)

    # 1. Shape
    print(f"Shape: {df.shape}  (rows={df.shape[0]}, columns={df.shape[1]})")

    # 2. Column names
    print("\nColumns:")
    for col in df.columns:
        print(f"  - {col}")

    # 3. Class distribution
    print("\nClass distribution:")
    counts = df["Class"].value_counts().sort_index()
    total = len(df)
    label_map = {0: "non-fraud", 1: "fraud"}
    for class_value, count in counts.items():
        label = label_map.get(int(class_value), str(class_value))
        pct = (count / total) * 100
        print(f"  {label} (Class={class_value}): {count:,} ({pct:.4f}%)")

    # 4. Missing values
    missing_total = int(df.isnull().sum().sum())
    if missing_total == 0:
        print("\nMissing values: none")
    else:
        print(f"\nMissing values: {missing_total} total")
        per_col = df.isnull().sum()
        per_col = per_col[per_col > 0]
        for col, n in per_col.items():
            print(f"  - {col}: {n}")


if __name__ == "__main__":
    main()

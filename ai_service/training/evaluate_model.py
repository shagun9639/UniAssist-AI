"""
UniAssist AI - Standalone Model Evaluation Script
Loads the serialized model and evaluates performance against test data,
generating summary statistics and verification output.
"""

import os
import sys
import json
import joblib
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
AI_SERVICE_DIR = os.path.abspath(os.path.join(CURRENT_DIR, ".."))
if AI_SERVICE_DIR not in sys.path:
    sys.path.insert(0, AI_SERVICE_DIR)

from app.preprocessing import clean_text


def run_evaluation():
    model_path = os.path.join(AI_SERVICE_DIR, "models", "model_pipeline.joblib")
    data_path = os.path.join(AI_SERVICE_DIR, "data", "intents.json")

    if not os.path.exists(model_path):
        print(f"Error: Model file not found at {model_path}. Run training first via python -m training.train_model")
        sys.exit(1)

    print(f"Loading trained pipeline from {model_path}...")
    pipeline = joblib.load(model_path)

    print(f"Loading dataset from {data_path}...")
    with open(data_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    texts = []
    labels = []
    for item in data.get("intents", []):
        intent = item["intent"]
        for pattern in item.get("patterns", []):
            cleaned = clean_text(pattern)
            if cleaned:
                texts.append(cleaned)
                labels.append(intent)

    _, X_test, _, y_test = train_test_split(
        texts, labels, test_size=0.20, random_state=42, stratify=labels
    )

    y_pred = pipeline.predict(X_test)
    accuracy = accuracy_score(y_test, y_pred)

    print("\n================== INDEPENDENT EVALUATION REPORT ==================")
    print(f"Test Samples Evaluated: {len(X_test)}")
    print(f"Measured Test Accuracy: {accuracy * 100:.2f}%")
    print("-------------------------------------------------------------------")
    print(classification_report(y_test, y_pred, zero_division=0))
    print("===================================================================\n")


if __name__ == "__main__":
    run_evaluation()

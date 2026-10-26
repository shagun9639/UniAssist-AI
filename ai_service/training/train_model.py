"""
UniAssist AI - Machine Learning Training Pipeline
Trains, evaluates, compares, and serializes the intent classification model
using TF-IDF feature extraction with Logistic Regression and Calibrated Linear SVC.
"""

import os
import sys
import json
import joblib
import numpy as np
import matplotlib
matplotlib.use("Agg")  # Headless backend for server/CLI execution
import matplotlib.pyplot as plt
from datetime import datetime
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.svm import LinearSVC
from sklearn.calibration import CalibratedClassifierCV
from sklearn.pipeline import Pipeline
from sklearn.metrics import (
    accuracy_score,
    precision_recall_fscore_support,
    classification_report,
    confusion_matrix
)

# Ensure app module can be imported
CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
AI_SERVICE_DIR = os.path.abspath(os.path.join(CURRENT_DIR, ".."))
if AI_SERVICE_DIR not in sys.path:
    sys.path.insert(0, AI_SERVICE_DIR)

from app.preprocessing import clean_text


def load_dataset(dataset_path: str):
    """Loads and preprocesses intent examples from intents.json."""
    with open(dataset_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    texts = []
    labels = []
    intent_metadata = {}

    for item in data.get("intents", []):
        intent_name = item["intent"]
        intent_metadata[intent_name] = {
            "description": item.get("description", ""),
            "category": item.get("category", "general"),
            "default_response": item.get("default_response", ""),
            "source": item.get("source", "University Support Handbook")
        }
        for pattern in item.get("patterns", []):
            cleaned = clean_text(pattern)
            if cleaned:
                texts.append(cleaned)
                labels.append(intent_name)

    return texts, labels, intent_metadata


def train_and_evaluate():
    data_path = os.path.join(AI_SERVICE_DIR, "data", "intents.json")
    models_dir = os.path.join(AI_SERVICE_DIR, "models")
    eval_dir = os.path.join(AI_SERVICE_DIR, "evaluation")
    os.makedirs(models_dir, exist_ok=True)
    os.makedirs(eval_dir, exist_ok=True)

    print(f"[1/6] Loading intent dataset from {data_path}...")
    texts, labels, intent_meta = load_dataset(data_path)
    unique_classes = sorted(list(set(labels)))
    print(f"      Loaded {len(texts)} samples across {len(unique_classes)} distinct intent classes.")

    # Stratified Train/Test split (80% train, 20% test)
    print("[2/6] Performing stratified train-test split (80/20)...")
    X_train, X_test, y_train, y_test = train_test_split(
        texts, labels, test_size=0.20, random_state=42, stratify=labels
    )
    print(f"      Training samples: {len(X_train)}, Testing samples: {len(X_test)}")

    # Model A: TF-IDF + Logistic Regression
    print("[3/6] Training Model A: TF-IDF + Logistic Regression...")
    pipeline_lr = Pipeline([
        ("tfidf", TfidfVectorizer(ngram_range=(1, 2), min_df=1, sublinear_tf=True)),
        ("clf", LogisticRegression(C=1.0, max_iter=1000, random_state=42))
    ])
    pipeline_lr.fit(X_train, y_train)
    y_pred_lr = pipeline_lr.predict(X_test)
    acc_lr = accuracy_score(y_test, y_pred_lr)
    p_lr, r_lr, f1_lr, _ = precision_recall_fscore_support(y_test, y_pred_lr, average="macro", zero_division=0)
    _, _, f1_w_lr, _ = precision_recall_fscore_support(y_test, y_pred_lr, average="weighted", zero_division=0)

    # Model B: TF-IDF + Calibrated Linear Support Vector Classifier
    print("[4/6] Training Model B: TF-IDF + Calibrated Linear SVC...")
    base_svc = LinearSVC(C=1.0, random_state=42)
    calibrated_svc = CalibratedClassifierCV(estimator=base_svc, cv=3)
    pipeline_svc = Pipeline([
        ("tfidf", TfidfVectorizer(ngram_range=(1, 2), min_df=1, sublinear_tf=True)),
        ("clf", calibrated_svc)
    ])
    pipeline_svc.fit(X_train, y_train)
    y_pred_svc = pipeline_svc.predict(X_test)
    acc_svc = accuracy_score(y_test, y_pred_svc)
    p_svc, r_svc, f1_svc, _ = precision_recall_fscore_support(y_test, y_pred_svc, average="macro", zero_division=0)
    _, _, f1_w_svc, _ = precision_recall_fscore_support(y_test, y_pred_svc, average="weighted", zero_division=0)

    print("\n=================== MODEL COMPARISON ===================")
    print(f"Model A (Logistic Regression): Acc={acc_lr:.4f}, Macro-P={p_lr:.4f}, Macro-R={r_lr:.4f}, Macro-F1={f1_lr:.4f}, Weighted-F1={f1_w_lr:.4f}")
    print(f"Model B (Calibrated Linear SVC): Acc={acc_svc:.4f}, Macro-P={p_svc:.4f}, Macro-R={r_svc:.4f}, Macro-F1={f1_svc:.4f}, Weighted-F1={f1_w_svc:.4f}")
    print("========================================================\n")

    # Select best model based on macro F1 score
    if f1_svc >= f1_lr:
        best_name = "Calibrated Linear SVC (Model B)"
        best_model = pipeline_svc
        best_acc = acc_svc
        best_macro_p = p_svc
        best_macro_r = r_svc
        best_macro_f1 = f1_svc
        best_weighted_f1 = f1_w_svc
        best_preds = y_pred_svc
    else:
        best_name = "Logistic Regression (Model A)"
        best_model = pipeline_lr
        best_acc = acc_lr
        best_macro_p = p_lr
        best_macro_r = r_lr
        best_macro_f1 = f1_lr
        best_weighted_f1 = f1_w_lr
        best_preds = y_pred_lr

    print(f"[5/6] Selected Best Model: {best_name}")

    # Generate detailed classification report
    report_dict = classification_report(y_test, best_preds, output_dict=True, zero_division=0)
    report_text = classification_report(y_test, best_preds, zero_division=0)
    print("\nDetailed Test Classification Report:")
    print(report_text)

    # Save Confusion Matrix visualization
    cm = confusion_matrix(y_test, best_preds, labels=unique_classes)
    plt.figure(figsize=(14, 12))
    plt.imshow(cm, interpolation="nearest", cmap=plt.cm.Blues)
    plt.title(f"UniAssist AI - Intent Confusion Matrix ({best_name})", fontsize=14, pad=15)
    plt.colorbar()
    tick_marks = np.arange(len(unique_classes))
    plt.xticks(tick_marks, unique_classes, rotation=60, ha="right", fontsize=9)
    plt.yticks(tick_marks, unique_classes, fontsize=9)

    # Annotate counts inside cells
    thresh = cm.max() / 2.0
    for i in range(cm.shape[0]):
        for j in range(cm.shape[1]):
            val = cm[i, j]
            if val > 0:
                plt.text(j, i, format(val, "d"),
                         ha="center", va="center",
                         color="white" if val > thresh else "black",
                         fontsize=8)

    plt.tight_layout()
    plt.ylabel("True Intent Class", fontsize=11)
    plt.xlabel("Predicted Intent Class", fontsize=11)
    cm_path = os.path.join(eval_dir, "confusion_matrix.png")
    plt.savefig(cm_path, dpi=200, bbox_inches="tight")
    plt.close()
    print(f"      Saved confusion matrix visualization to {cm_path}")

    # Save model comparison summary
    comparison_data = {
        "evaluation_timestamp": datetime.now().isoformat(),
        "total_samples": len(texts),
        "train_samples": len(X_train),
        "test_samples": len(X_test),
        "num_classes": len(unique_classes),
        "models": {
            "logistic_regression": {
                "name": "TF-IDF + Logistic Regression",
                "accuracy": round(float(acc_lr), 4),
                "macro_precision": round(float(p_lr), 4),
                "macro_recall": round(float(r_lr), 4),
                "macro_f1": round(float(f1_lr), 4),
                "weighted_f1": round(float(f1_w_lr), 4)
            },
            "calibrated_linear_svc": {
                "name": "TF-IDF + Calibrated Linear SVC",
                "accuracy": round(float(acc_svc), 4),
                "macro_precision": round(float(p_svc), 4),
                "macro_recall": round(float(r_svc), 4),
                "macro_f1": round(float(f1_svc), 4),
                "weighted_f1": round(float(f1_w_svc), 4)
            }
        },
        "selected_model": best_name
    }
    with open(os.path.join(eval_dir, "model_comparison.json"), "w", encoding="utf-8") as f:
        json.dump(comparison_data, f, indent=2)

    # Save comprehensive metrics JSON
    metrics_data = {
        "model_name": best_name,
        "model_version": "v1.0.0",
        "evaluation_timestamp": datetime.now().isoformat(),
        "test_accuracy": round(float(best_acc), 4),
        "macro_precision": round(float(best_macro_p), 4),
        "macro_recall": round(float(best_macro_r), 4),
        "macro_f1": round(float(best_macro_f1), 4),
        "weighted_f1": round(float(best_weighted_f1), 4),
        "classification_report": report_dict,
        "classes": unique_classes
    }
    with open(os.path.join(eval_dir, "metrics.json"), "w", encoding="utf-8") as f:
        json.dump(metrics_data, f, indent=2)

    # Serialize best model pipeline
    model_output_path = os.path.join(models_dir, "model_pipeline.joblib")
    joblib.dump(best_model, model_output_path)
    print(f"[6/6] Saved trained model pipeline to {model_output_path}")

    # Save model metadata
    metadata = {
        "model_name": "UniAssist Intent Classifier",
        "algorithm": best_name,
        "version": "v1.0.0",
        "trained_at": datetime.now().isoformat(),
        "total_dataset_size": len(texts),
        "train_size": len(X_train),
        "test_size": len(X_test),
        "test_accuracy": round(float(best_acc), 4),
        "macro_f1": round(float(best_macro_f1), 4),
        "weighted_f1": round(float(best_weighted_f1), 4),
        "classes": unique_classes,
        "intent_metadata": intent_meta,
        "pipeline_file": "model_pipeline.joblib"
    }
    with open(os.path.join(models_dir, "model_metadata.json"), "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2)

    print("\nModel training and evaluation successfully completed!")
    return metrics_data


if __name__ == "__main__":
    train_and_evaluate()

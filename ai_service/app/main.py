"""
UniAssist AI - Python FastAPI Microservice
Exposes REST APIs for NLP intent classification, FAQ grounding,
model evaluation analytics, and feedback ingestion.
"""

import os
import json
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from app.schemas import (
    PredictRequest,
    PredictResponse,
    ChatRequest,
    ChatResponse,
    FeedbackRequest,
    FeedbackResponse,
    ModelInfoResponse
)
from app.inference import inference_service

app = FastAPI(
    title="UniAssist AI - University Support AI/ML Service",
    description="NLP microservice providing intent classification, entity extraction, and FAQ retrieval for student support.",
    version="1.0.0"
)

# Enable CORS for frontend and Java backend communication
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
AI_SERVICE_DIR = os.path.abspath(os.path.join(CURRENT_DIR, ".."))


@app.get("/health", tags=["Health"])
def health_check():
    """Returns the operational status and loaded model information."""
    is_loaded = inference_service.model is not None
    return {
        "status": "healthy" if is_loaded else "degraded",
        "service": "UniAssist AI Microservice",
        "model_loaded": is_loaded,
        "version": inference_service.metadata.get("version", "v1.0.0"),
        "algorithm": inference_service.metadata.get("algorithm", "Calibrated Linear SVC")
    }


@app.post("/predict", response_model=PredictResponse, tags=["ML Inference"])
def predict_intent(request: PredictRequest):
    """Predicts intent class, confidence score, and extracted entities."""
    try:
        intent, confidence, entities = inference_service.predict(request.text)
        is_fallback = confidence < 0.25
        return PredictResponse(
            intent=intent,
            confidence=confidence,
            entities=entities,
            fallback=is_fallback
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Inference error: {str(e)}")


@app.post("/chat", response_model=ChatResponse, tags=["Chatbot"])
def chat(request: ChatRequest):
    """
    End-to-end student chat processing endpoint.
    Extracts intent, queries FAQ knowledge base, verifies confidence,
    and returns grounded answer with official sources.
    """
    try:
        result = inference_service.generate_chat_response(request.message)
        return ChatResponse(
            intent=result["intent"],
            confidence=result["confidence"],
            entities=result["entities"],
            answer=result["answer"],
            source=result["source"],
            fallback=result["fallback"],
            suggested_followups=result["suggested_followups"]
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Chat processing error: {str(e)}")


@app.post("/feedback", response_model=FeedbackResponse, tags=["Feedback"])
def record_feedback(request: FeedbackRequest):
    """Records student feedback (helpful/not helpful) on model answers."""
    try:
        feedback_log_path = os.path.join(AI_SERVICE_DIR, "data", "feedback_log.jsonl")
        log_entry = {
            "query": request.query,
            "intent": request.intent,
            "rating": request.rating,
            "comments": request.comments,
            "message_id": request.message_id
        }
        with open(feedback_log_path, "a", encoding="utf-8") as f:
            f.write(json.dumps(log_entry) + "\n")

        return FeedbackResponse(
            status="success",
            message="Thank you! Your feedback has been safely logged for administrative review and model improvement."
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to record feedback: {str(e)}")


@app.get("/model-info", response_model=ModelInfoResponse, tags=["Model Analytics"])
def get_model_info():
    """Returns model metadata, training details, and supported intent classes."""
    meta = inference_service.metadata
    if not meta:
        raise HTTPException(status_code=404, detail="Model metadata is unavailable. Train model first.")

    return ModelInfoResponse(
        model_name=meta.get("model_name", "UniAssist Intent Classifier"),
        version=meta.get("version", "v1.0.0"),
        algorithm=meta.get("algorithm", "Calibrated Linear SVC"),
        trained_at=meta.get("trained_at", "Unknown"),
        total_dataset_size=meta.get("total_dataset_size", 0),
        test_accuracy=meta.get("test_accuracy", 0.0),
        macro_f1=meta.get("macro_f1", 0.0),
        supported_intents=meta.get("classes", [])
    )


@app.get("/model-metrics", tags=["Model Analytics"])
def get_model_metrics():
    """Returns detailed classification report and per-intent evaluation metrics."""
    metrics_path = os.path.join(AI_SERVICE_DIR, "evaluation", "metrics.json")
    if not os.path.exists(metrics_path):
        raise HTTPException(status_code=404, detail="Evaluation metrics not found.")

    with open(metrics_path, "r", encoding="utf-8") as f:
        return json.load(f)


@app.get("/model-comparison", tags=["Model Analytics"])
def get_model_comparison():
    """Returns comparative metrics between Logistic Regression and Calibrated Linear SVC."""
    comparison_path = os.path.join(AI_SERVICE_DIR, "evaluation", "model_comparison.json")
    if not os.path.exists(comparison_path):
        raise HTTPException(status_code=404, detail="Model comparison data not found.")

    with open(comparison_path, "r", encoding="utf-8") as f:
        return json.load(f)


@app.get("/confusion-matrix", tags=["Model Analytics"])
def get_confusion_matrix():
    """Serves the generated confusion matrix PNG image."""
    cm_path = os.path.join(AI_SERVICE_DIR, "evaluation", "confusion_matrix.png")
    if not os.path.exists(cm_path):
        raise HTTPException(status_code=404, detail="Confusion matrix image not found.")

    return FileResponse(cm_path, media_type="image/png")

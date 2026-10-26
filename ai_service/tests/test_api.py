"""
UniAssist AI - Tests for FastAPI Endpoints
"""

import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert data["model_loaded"] is True


def test_predict_endpoint():
    response = client.post("/predict", json={"text": "Where is the library located?"})
    assert response.status_code == 200
    data = response.json()
    assert "intent" in data
    assert "confidence" in data
    assert data["confidence"] >= 0.0


def test_chat_endpoint():
    response = client.post("/chat", json={"message": "What is the minimum attendance requirement?"})
    assert response.status_code == 200
    data = response.json()
    assert data["intent"] == "attendance_policy"
    assert "answer" in data
    assert "source" in data
    assert isinstance(data["suggested_followups"], list)


def test_feedback_endpoint():
    payload = {
        "query": "Where can I find my timetable?",
        "intent": "timetable",
        "rating": "helpful",
        "comments": "Accurate response"
    }
    response = client.post("/feedback", json=payload)
    assert response.status_code == 200
    assert response.json()["status"] == "success"


def test_model_info_endpoint():
    response = client.get("/model-info")
    assert response.status_code == 200
    data = response.json()
    assert data["version"] == "v1.0.0"
    assert len(data["supported_intents"]) >= 15

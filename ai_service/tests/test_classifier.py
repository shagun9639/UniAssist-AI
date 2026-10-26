"""
UniAssist AI - Tests for Intent Classifier and Inference Logic
"""

import pytest
from app.inference import inference_service


def test_model_loaded():
    assert inference_service.model is not None
    assert len(inference_service.metadata.get("classes", [])) >= 15


def test_predict_attendance_intent():
    query = "What is the mandatory attendance percentage required to sit for exams?"
    intent, confidence, entities = inference_service.predict(query)
    assert intent == "attendance_policy"
    assert confidence > 0.30


def test_predict_fee_intent():
    query = "How do I pay my semester tuition fee online?"
    intent, confidence, _ = inference_service.predict(query)
    assert intent == "fee_payment"
    assert confidence > 0.30


def test_predict_greeting():
    query = "Hello good morning UniAssist"
    intent, confidence, _ = inference_service.predict(query)
    assert intent == "greeting"


def test_generate_chat_response():
    query = "What is the attendance rule?"
    resp = inference_service.generate_chat_response(query)
    assert resp["intent"] == "attendance_policy"
    assert "attendance" in resp["answer"].lower()
    assert resp["source"] != ""
    assert resp["fallback"] is False
    assert len(resp["suggested_followups"]) > 0


def test_empty_query_fallback():
    resp = inference_service.generate_chat_response("")
    assert resp["fallback"] is True

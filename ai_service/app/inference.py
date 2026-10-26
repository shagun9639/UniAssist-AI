"""
UniAssist AI - Model Inference and Chat Orchestration
Executes intent prediction, entity extraction, FAQ retrieval,
confidence threshold checks, and structured response synthesis.
"""

import os
import json
import joblib
import numpy as np
from typing import Dict, Any, Tuple
from app.preprocessing import clean_text, extract_entities
from app.retrieval import retrieval_engine

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
AI_SERVICE_DIR = os.path.abspath(os.path.join(CURRENT_DIR, ".."))
MODEL_PATH = os.path.join(AI_SERVICE_DIR, "models", "model_pipeline.joblib")
META_PATH = os.path.join(AI_SERVICE_DIR, "models", "model_metadata.json")

# Calibrated confidence threshold for out-of-distribution or uncertain queries
CONFIDENCE_THRESHOLD = 0.25


class IntentInferenceService:
    def __init__(self):
        self.model = None
        self.metadata = {}
        self.intent_metadata = {}
        self.load_model()

    def load_model(self):
        if not os.path.exists(MODEL_PATH):
            raise FileNotFoundError(
                f"Trained model not found at {MODEL_PATH}. Run 'python -m training.train_model' to generate artifacts."
            )

        print(f"Loading model pipeline from {MODEL_PATH}...")
        self.model = joblib.load(MODEL_PATH)

        if os.path.exists(META_PATH):
            with open(META_PATH, "r", encoding="utf-8") as f:
                self.metadata = json.load(f)
                self.intent_metadata = self.metadata.get("intent_metadata", {})

    def predict(self, raw_text: str) -> Tuple[str, float, Dict[str, Any]]:
        """Predicts intent class and probability confidence score."""
        cleaned = clean_text(raw_text)
        entities = extract_entities(raw_text)

        if not cleaned:
            return "unrecognized_query", 0.0, entities

        # Calibrated classifier supports predict_proba
        if hasattr(self.model, "predict_proba"):
            probs = self.model.predict_proba([cleaned])[0]
            max_idx = np.argmax(probs)
            confidence = float(probs[max_idx])
            classes = self.model.classes_
            predicted_intent = str(classes[max_idx])
        else:
            predicted_intent = str(self.model.predict([cleaned])[0])
            confidence = 0.85

        return predicted_intent, round(confidence, 4), entities

    def generate_chat_response(self, raw_text: str) -> Dict[str, Any]:
        """
        Orchestrates full student query workflow:
        Preprocessing -> Intent -> Entity Extraction -> Confidence Evaluation -> FAQ Retrieval.
        """
        predicted_intent, confidence, entities = self.predict(raw_text)

        # Generate contextual follow-up suggestions
        followups_map = {
            "attendance_policy": [
                "How do I submit a medical leave certificate?",
                "Can I sit for semester exams with 65% attendance?",
                "Where can I check my current attendance percentage?"
            ],
            "exam_schedule": [
                "When will the examination admit cards be issued?",
                "What is the reporting time for morning exam sessions?",
                "How do I resolve exam paper timetable clashes?"
            ],
            "exam_results": [
                "What is the fee and procedure for re-evaluation?",
                "How is cumulative CGPA calculated from SGPA?",
                "What is the passing grade requirement in a subject?"
            ],
            "admissions": [
                "What are the eligibility criteria for B.Tech admission?",
                "Is direct admission available through lateral entry?",
                "What is the last date to submit the application form?"
            ],
            "admission_eligibility": [
                "What is the minimum percentage required in Class 12?",
                "Do I need a valid JEE Main score for engineering?",
                "Are scholarships available for high-ranking students?"
            ],
            "fee_payment": [
                "What are the accepted online payment methods?",
                "What is the late fee penalty per day?",
                "Where can I download my official tuition fee receipt?"
            ],
            "scholarships": [
                "What is the minimum CGPA to retain merit scholarship?",
                "How can I apply for post-matric government scholarships?",
                "Where do I submit my family income certificate?"
            ],
            "timetable": [
                "When do morning lectures and practical labs start?",
                "Where can I find the 4th semester timetable?",
                "How are practical laboratory batches divided?"
            ],
            "course_information": [
                "How many credits are required to graduate in B.Tech?",
                "Where can I download the complete syllabus PDF?",
                "What elective specializations are offered?"
            ],
            "library_facilities": [
                "What are the library timings during exam weeks?",
                "How many books can an undergraduate student borrow?",
                "How do I access IEEE and Springer e-journals online?"
            ],
            "hostel_information": [
                "What are the in-campus hostel curfew timings?",
                "What are the mess timings for breakfast and dinner?",
                "How do I report room maintenance issues?"
            ],
            "transport_facilities": [
                "What are the university bus departure timings?",
                "How much is the semester bus transportation fee?",
                "Where can I get my student bus pass renewed?"
            ],
            "technical_support": [
                "How do I reset my student portal login password?",
                "Where is the campus IT helpdesk office located?",
                "How do I connect to the university eduroam Wi-Fi?"
            ],
            "faculty_contacts": [
                "When are faculty consultation office hours held?",
                "Where can I find my department head's email?",
                "Where is the Dean of Academic Affairs office?"
            ],
            "academic_calendar": [
                "When does the autumn semester officially start?",
                "What are the dates for the mid-semester break?",
                "When will classes end for the current academic session?"
            ]
        }

        suggested_followups = followups_map.get(predicted_intent, [
            "What is the attendance policy?",
            "When are semester exams scheduled?",
            "How can I pay my tuition fees?",
            "Where is the university library located?"
        ])

        # Check for low confidence / unknown query fallback
        is_fallback = confidence < CONFIDENCE_THRESHOLD

        if is_fallback:
            fallback_answer = (
                "I am not entirely confident in answering that specific question. "
                "To ensure you receive accurate and verified university information, "
                "please rephrase your query or browse our official FAQ sections: "
                "Admissions, Attendance Rules, Examinations, Fees, Scholarships, or Campus Facilities. "
                "You can also submit an official support ticket for staff assistance."
            )
            return {
                "intent": "low_confidence_fallback",
                "confidence": confidence,
                "entities": entities,
                "answer": fallback_answer,
                "source": "UniAssist Verification Safety Policy",
                "fallback": True,
                "suggested_followups": [
                    "What is the minimum attendance requirement?",
                    "Where can I download my exam date sheet?",
                    "How do I pay my tuition fees online?",
                    "Submit a support ticket"
                ]
            }

        # Retrieve verified FAQ grounded in university knowledge base
        matched_faq = retrieval_engine.retrieve(raw_text, predicted_intent)
        if matched_faq:
            answer = matched_faq["answer"]
            source = f"University FAQ #{matched_faq['faq_id']} - {matched_faq['category'].capitalize()}"
        else:
            # Fall back to default intent response from dataset
            intent_info = self.intent_metadata.get(predicted_intent, {})
            answer = intent_info.get(
                "default_response",
                "Please consult the relevant university department portal for official guidance on this topic."
            )
            source = intent_info.get("source", "University Support Handbook")

        return {
            "intent": predicted_intent,
            "confidence": confidence,
            "entities": entities,
            "answer": answer,
            "source": source,
            "fallback": False,
            "suggested_followups": suggested_followups
        }


# Global singleton instance
inference_service = IntentInferenceService()

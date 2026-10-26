"""
UniAssist AI - University Knowledge Base & FAQ Retrieval Engine
Uses TF-IDF similarity and keyword matching over university FAQs
to ground answers in verified official policy documents.
"""

import os
import pandas as pd
from typing import Dict, Any, Optional
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from app.preprocessing import clean_text

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
AI_SERVICE_DIR = os.path.abspath(os.path.join(CURRENT_DIR, ".."))
FAQS_PATH = os.path.join(AI_SERVICE_DIR, "data", "faqs.csv")


class FAQRetrievalEngine:
    def __init__(self, faqs_path: str = FAQS_PATH):
        self.faqs_path = faqs_path
        self.faqs_df = pd.DataFrame()
        self.vectorizer = TfidfVectorizer(ngram_range=(1, 2), sublinear_tf=True)
        self.tfidf_matrix = None
        self._load_and_index()

    def _load_and_index(self):
        if not os.path.exists(self.faqs_path):
            print(f"Warning: FAQ dataset not found at {self.faqs_path}")
            return

        self.faqs_df = pd.read_csv(self.faqs_path)
        # Combine question, keywords, and answer for rich semantic matching
        documents = []
        for _, row in self.faqs_df.iterrows():
            q = str(row.get("question", ""))
            kw = str(row.get("keywords", ""))
            ans = str(row.get("answer", ""))
            combined = f"{q} {kw} {ans}"
            documents.append(clean_text(combined))

        if documents:
            self.tfidf_matrix = self.vectorizer.fit_transform(documents)

    def retrieve(self, query: str, intent: Optional[str] = None) -> Optional[Dict[str, Any]]:
        """
        Retrieves the most relevant verified FAQ entry for the student query.
        Applies intent category boosting when the intent is known.
        """
        if self.faqs_df.empty or self.tfidf_matrix is None or not query.strip():
            return None

        cleaned_query = clean_text(query)
        if not cleaned_query:
            return None

        query_vec = self.vectorizer.transform([cleaned_query])
        similarities = cosine_similarity(query_vec, self.tfidf_matrix).flatten()

        # Intent-to-category mapping for boosting
        intent_category_map = {
            "admissions": "admissions",
            "admission_eligibility": "admissions",
            "course_information": "academics",
            "timetable": "academics",
            "attendance_policy": "academics",
            "academic_calendar": "academics",
            "assignments": "academics",
            "exam_schedule": "examinations",
            "exam_results": "examinations",
            "fee_payment": "finance",
            "scholarships": "finance",
            "library_facilities": "campus",
            "hostel_information": "campus",
            "transport_facilities": "campus",
            "campus_amenities": "campus",
            "faculty_contacts": "support",
            "technical_support": "support"
        }

        target_category = intent_category_map.get(intent) if intent else None

        best_score = -1.0
        best_idx = -1

        for idx, sim in enumerate(similarities):
            row = self.faqs_df.iloc[idx]
            if not row.get("active", True):
                continue

            score = float(sim)
            # Boost score if category matches predicted intent
            if target_category and row.get("category", "") == target_category:
                score += 0.20

            if score > best_score:
                best_score = score
                best_idx = idx

        # Return best match if similarity threshold is met
        if best_idx >= 0 and best_score >= 0.15:
            matched_row = self.faqs_df.iloc[best_idx]
            return {
                "faq_id": int(matched_row.get("id", best_idx + 1)),
                "question": str(matched_row.get("question", "")),
                "answer": str(matched_row.get("answer", "")),
                "category": str(matched_row.get("category", "")),
                "source_url": str(matched_row.get("source_url", "")),
                "retrieval_score": round(best_score, 4)
            }

        return None


# Global singleton instance
retrieval_engine = FAQRetrievalEngine()

# Model Evaluation & Error Analysis Report

## UniAssist AI – Natural Language Intent Classification Evaluation

---

## 1. Executive Summary

The natural language intent classification engine of **UniAssist AI** was evaluated on an 80/20 stratified split of the university query dataset. Out of **408 total labeled samples**, **82 queries** were reserved as a strictly held-out test partition. 

The production model, **Calibrated Linear Support Vector Classifier (Model B)**, achieved a **70.73% test accuracy** and a **macro-averaged F1-score of 0.6825**, decisively outperforming the Logistic Regression baseline (64.63% accuracy).

---

## 2. Algorithm Tournament & Comparative Metrics

Both candidate models were evaluated under identical conditions using the exact same preprocessed text features and stratified test split.

| Evaluation Metric | Model A: Logistic Regression | Model B: Calibrated Linear SVC | Improvement (Delta) |
| :--- | :-: | :-: | :-: |
| **Test Accuracy** | 64.63% | **70.73%** | **+6.10%** |
| **Macro Precision** | 0.6864 | **0.7058** | **+1.94%** |
| **Macro Recall** | 0.6425 | **0.7050** | **+6.25%** |
| **Macro F1-Score** | 0.6375 | **0.6825** | **+4.50%** |
| **Weighted F1-Score** | 0.6409 | **0.6864** | **+4.55%** |
| **Inference Latency** | ~12 ms | **~14 ms** | Negligible |
| **Decision** | Baseline | **Selected Production Model** | **Champion** |

---

## 3. Detailed Per-Intent Classification Breakdown

The table below details precision, recall, F1-score, and test support across all 20 intent classes for Model B on held-out test data:

| Intent Class Identifier | Precision | Recall | F1-Score | Support (N) | Performance Category |
| :--- | :-: | :-: | :-: | :-: | :--- |
| `attendance_policy` | 1.0000 | 1.0000 | **1.0000** | 4 | Perfect (100%) |
| `library_facilities` | 1.0000 | 1.0000 | **1.0000** | 4 | Perfect (100%) |
| `admissions` | 0.8000 | 1.0000 | **0.8889** | 4 | High Performance |
| `greeting` | 1.0000 | 0.8000 | **0.8889** | 5 | High Performance |
| `timetable` | 1.0000 | 0.7500 | **0.8571** | 4 | High Performance |
| `goodbye` | 0.8000 | 0.8000 | **0.8000** | 5 | High Performance |
| `university_holidays` | 0.6667 | 1.0000 | **0.8000** | 4 | High Performance |
| `exam_schedule` | 0.7500 | 0.7500 | **0.7500** | 4 | Robust |
| `faculty_contacts` | 0.7500 | 0.7500 | **0.7500** | 4 | Robust |
| `fee_payment` | 0.7500 | 0.7500 | **0.7500** | 4 | Robust |
| `transport_facilities` | 0.5714 | 1.0000 | **0.7273** | 4 | Robust |
| `admission_eligibility` | 0.6000 | 0.7500 | **0.6667** | 4 | Moderate |
| `technical_support` | 0.6000 | 0.7500 | **0.6667** | 4 | Moderate |
| `course_information` | 0.6667 | 0.5000 | **0.5714** | 4 | Moderate |
| `hostel_information` | 0.6667 | 0.5000 | **0.5714** | 4 | Moderate |
| `scholarships` | 0.6667 | 0.5000 | **0.5714** | 4 | Moderate |
| `assignments` | 0.4286 | 0.7500 | **0.5455** | 4 | Boundary Overlap |
| `campus_amenities` | 0.4000 | 0.5000 | **0.4444** | 4 | Boundary Overlap |
| `academic_calendar` | 1.0000 | 0.2500 | **0.4000** | 4 | Vocabulary Overlap |
| `exam_results` | 0.0000 | 0.0000 | **0.0000** | 4 | Cross-Class Ambiguity |
| **Macro Average** | **0.7058** | **0.7050** | **0.6825** | **82** | |
| **Weighted Average** | **0.7106** | **0.7073** | **0.6864** | **82** | |

---

## 4. Qualitative Error Analysis & Confusion Matrix Insights

A comprehensive inspection of test predictions and the generated confusion matrix (`ai_service/evaluation/confusion_matrix.png`) revealed three specific root causes of classification errors:

### 4.1 Cross-Class Lexical Ambiguity: `exam_results` vs. `exam_schedule`
- **Observed Behavior:** Test queries intended for `exam_results` (e.g., *"When will final exam results be published?"*) were misclassified as `exam_schedule`.
- **Root Cause:** Both intents share high TF-IDF feature weights for terms like `["exam", "examination", "semester", "date", "when"]`. In short queries without explicit mentions of *"grades"*, *"GPA"*, or *"marksheet"*, the classifier leans toward the higher-frequency `exam_schedule` hyperplane.
- **Architectural Solution in UniAssist AI:** Rather than relying solely on intent output, UniAssist AI implements a **hybrid FAQ retrieval layer**. The retrieval engine computes cosine similarity directly against verified FAQ questions (e.g., *"How do I check my semester examination results?"*) and boosts matching FAQ categories. This ensures that even when the raw intent is borderline, the student still receives the correct answer.

### 4.2 Shared Facility Vocabulary: `campus_amenities` vs. `hostel_information`
- **Observed Behavior:** Queries regarding campus dining facilities or cafeteria operating hours were occasionally predicted as hostel mess facilities.
- **Root Cause:** Words such as *"food"*, *"mess"*, *"canteen"*, and *"lunch"* appear in training data across both campus amenities and student residential hostel sections.

### 4.3 Academic Periodicity: `academic_calendar` vs. `timetable`
- **Observed Behavior:** Queries inquiring about *"when does term start"* or *"vacation dates"* occasionally overlapped with weekly timetable schedule inquiries.

---

## 5. Architectural Mitigations & Safeguards

To prevent student frustration caused by statistical classification limitations, the following safeguards are implemented:

1. **Empirical Confidence Thresholding ($\tau = 0.25$):** Any prediction where the top calibrated posterior probability falls below 0.25 triggers a polite, context-aware fallback response rather than presenting an incorrect answer.
2. **Automated Query Escalation:** Queries triggering fallbacks are automatically persisted in the `unanswered_queries` database table. The administrative console provides a dedicated review view with single-click escalation to support tickets or new FAQ entries.
3. **One-Click Support Ticket Creation:** The chatbot interface includes an immediate action button enabling students to convert an unresolved query into a tracked support ticket handled by human staff.

---

## 6. Recommendations for Production Scaling

1. **Active Learning via Triage:** Continuously incorporate resolved entries from `unanswered_queries` into `intents.json` to expand boundary coverage.
2. **Sub-Intent Disambiguation:** Prompt students with interactive suggestion chips (e.g., *"Are you asking about Exam Dates or Exam Results?"*) when top two class probabilities are within a 0.05 margin.
3. **Continuous Retraining Pipeline:** Implement an automated retraining hook in the backend that rebuilds `model_pipeline.joblib` whenever 50+ new verified queries are approved by administrators.

"""
UniAssist AI - Unit tests for NLP preprocessing and entity extraction
"""

import pytest
from app.preprocessing import clean_text, extract_entities


def test_clean_text_basic():
    raw = "What is the Attendance requirement for B.Tech CSE???"
    cleaned = clean_text(raw)
    assert "attendance" in cleaned
    assert "btech" in cleaned
    assert "cse" in cleaned
    assert "?" not in cleaned


def test_clean_text_contractions():
    raw = "I can't attend lectures tomorrow. Won't I be debarred?"
    cleaned = clean_text(raw)
    assert "can not" in cleaned
    assert "will not" in cleaned


def test_clean_text_semester_normalization():
    assert clean_text("Exam date for sem 4") == "exam date for semester 4"
    assert clean_text("Schedule for 6th semester") == "schedule for semester 6"


def test_extract_entities():
    query = "Can 3rd sem B.Tech students in CSE apply for scholarship?"
    entities = extract_entities(query)
    assert entities.get("semester") == 3
    assert entities.get("course") == "B.Tech"
    assert entities.get("department") == "Computer Science & Engineering"


def test_clean_text_empty_input():
    assert clean_text("") == ""
    assert clean_text(None) == ""

"""
UniAssist AI - NLP Preprocessing Pipeline
Provides consistent text normalization, token cleaning, and entity preservation
used during both model training and runtime inference.
"""

import re
import unicodedata
from typing import Dict, Any


def clean_text(text: str) -> str:
    """
    Cleans and normalizes incoming student query text.
    Preserves meaningful negations ('not', 'no', 'without'),
    academic abbreviations ('B.Tech', 'M.Tech', 'Ph.D'), and numeric tokens.
    """
    if not text or not isinstance(text, str):
        return ""

    # Normalize unicode (e.g. smart quotes, accented chars)
    normalized = unicodedata.normalize("NFKD", text)

    # Convert to lowercase
    lower = normalized.lower().strip()

    # Expand common contractions to preserve semantics
    contractions = {
        r"can't": "can not",
        r"won't": "will not",
        r"n't": " not",
        r"'re": " are",
        r"'s": " is",
        r"'d": " would",
        r"'ll": " will",
        r"'ve": " have",
        r"'m": " am",
    }
    for pattern, replacement in contractions.items():
        lower = re.sub(pattern, replacement, lower)

    # Standardize common academic terms
    lower = re.sub(r"\bb\.?tech\b", "btech", lower)
    lower = re.sub(r"\bm\.?tech\b", "mtech", lower)
    lower = re.sub(r"\bb\.?sc\b", "bsc", lower)
    lower = re.sub(r"\bm\.?sc\b", "msc", lower)
    lower = re.sub(r"\bm\.?c\.?a\b", "mca", lower)
    lower = re.sub(r"\bb\.?c\.?a\b", "bca", lower)
    lower = re.sub(r"\bm\.?b\.?a\b", "mba", lower)
    lower = re.sub(r"\bb\.?b\.?a\b", "bba", lower)
    lower = re.sub(r"\bph\.?d\b", "phd", lower)

    # Standardize semester references (e.g. "sem 4", "4th sem", "semester 4")
    lower = re.sub(r"\bsem(?:ester)?\s*([1-8])\b", r"semester \1", lower)
    lower = re.sub(r"\b([1-8])(?:st|nd|rd|th)\s*sem(?:ester)?\b", r"semester \1", lower)

    # Remove special punctuation while preserving letters, numbers, spaces, and hyphens
    cleaned = re.sub(r"[^\w\s\-]", " ", lower)

    # Collapse multiple whitespaces into a single space
    cleaned = re.sub(r"\s+", " ", cleaned).strip()

    return cleaned


def extract_entities(text: str) -> Dict[str, Any]:
    """
    Extracts high-level academic entities such as course, semester, and department
    using deterministic pattern recognition.
    """
    entities: Dict[str, Any] = {}
    if not text:
        return entities

    lower = text.lower()

    # Extract Semester
    sem_match = re.search(r"\b(?:semester|sem)\s*([1-8])\b|\b([1-8])(?:st|nd|rd|th)\s*(?:semester|sem)\b", lower)
    if sem_match:
        sem_num = sem_match.group(1) or sem_match.group(2)
        entities["semester"] = int(sem_num)

    # Extract Course
    course_patterns = [
        (r"\bb\.?tech\b", "B.Tech"),
        (r"\bm\.?tech\b", "M.Tech"),
        (r"\bm\.?c\.?a\b", "MCA"),
        (r"\bb\.?c\.?a\b", "BCA"),
        (r"\bm\.?b\.?a\b", "MBA"),
        (r"\bb\.?b\.?a\b", "BBA"),
        (r"\bph\.?d\b", "Ph.D"),
        (r"\bb\.?sc\b", "B.Sc"),
        (r"\bm\.?sc\b", "M.Sc")
    ]
    for pattern, formal_name in course_patterns:
        if re.search(pattern, lower):
            entities["course"] = formal_name
            break

    # Extract Department
    departments = {
        "cse": "Computer Science & Engineering",
        "computer science": "Computer Science & Engineering",
        "information technology": "Information Technology",
        "it": "Information Technology",
        "ece": "Electronics & Communication Engineering",
        "mechanical": "Mechanical Engineering",
        "civil": "Civil Engineering",
        "electrical": "Electrical Engineering",
        "management": "Department of Management Studies",
        "business": "Department of Management Studies"
    }
    for dept_key, formal_dept in departments.items():
        if re.search(r"\b" + dept_key + r"\b", lower):
            entities["department"] = formal_dept
            break

    return entities

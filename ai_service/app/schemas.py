"""
UniAssist AI - Pydantic Request and Response Schemas
Defines strict API contracts between the Python AI microservice and consumers.
"""

from pydantic import BaseModel, Field
from typing import Dict, Any, List, Optional


class PredictRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=1000, description="Student input query")


class PredictResponse(BaseModel):
    intent: str
    confidence: float
    entities: Dict[str, Any] = Field(default_factory=dict)
    fallback: bool = False


class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1, max_length=1000, description="Student message text")
    conversation_id: Optional[int] = Field(None, description="Optional conversation identifier")
    user_id: Optional[int] = Field(None, description="Optional user identifier")


class ChatResponse(BaseModel):
    intent: str
    confidence: float
    entities: Dict[str, Any] = Field(default_factory=dict)
    answer: str
    source: str
    fallback: bool
    suggested_followups: List[str] = Field(default_factory=list)


class FeedbackRequest(BaseModel):
    query: str
    intent: str
    rating: str = Field(..., description="'helpful' or 'not_helpful'")
    comments: Optional[str] = None
    message_id: Optional[int] = None


class FeedbackResponse(BaseModel):
    status: str
    message: str


class ModelInfoResponse(BaseModel):
    model_name: str
    version: str
    algorithm: str
    trained_at: str
    total_dataset_size: int
    test_accuracy: float
    macro_f1: float
    supported_intents: List[str]

from enum import Enum
from typing import List, Optional, Dict, Any
from pydantic import BaseModel, ConfigDict, Field


class ThreatType(str, Enum):
    NONE = "NONE"
    CREDENTIAL_STUFFING = "CREDENTIAL_STUFFING"
    BOLA = "BOLA"
    ID_ENUMERATION = "ID_ENUMERATION"
    SUSPICIOUS_BEHAVIOR = "SUSPICIOUS_BEHAVIOR"


class Severity(str, Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"


class RecommendedAction(str, Enum):
    ALLOW = "ALLOW"
    ALERT = "ALERT"
    BLOCK = "BLOCK"


def to_camel(string: str) -> str:
    components = string.split('_')
    return components[0] + ''.join(x.title() for x in components[1:])


class DetectionResult(BaseModel):
    event_id: str = Field(..., alias="eventId")
    detected: bool
    threat_type: ThreatType = Field(default=ThreatType.NONE, alias="threatType")
    confidence: float = Field(..., ge=0.0, le=1.0, description="Model classification certainty 0.0-1.0")
    risk_score: float = Field(..., ge=0.0, le=1.0, alias="riskScore", description="Dangerousness of behavior 0.0-1.0")
    severity: Severity
    recommended_action: RecommendedAction = Field(..., alias="recommendedAction")
    reason_codes: List[str] = Field(default_factory=list, alias="reasonCodes")
    details: Optional[Dict[str, Any]] = Field(default=None, description="Detailed evidence breakdown for explainability")

    model_config = ConfigDict(
        populate_by_name=True,
        alias_generator=to_camel
    )

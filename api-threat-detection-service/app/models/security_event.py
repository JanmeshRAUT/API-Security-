from typing import Optional, Dict, Any
from pydantic import BaseModel, ConfigDict, Field
from app.models.feature_set import BaseSchema


class SecurityEvent(BaseSchema):
    event_id: str = Field(..., alias="eventId")
    application_id: str = Field(..., alias="applicationId")
    timestamp: str
    request_uri: str = Field(..., alias="requestUri")
    method: str
    status_code: int = Field(..., alias="statusCode")
    source_ip: str = Field(..., alias="sourceIp")
    user_id: Optional[str] = Field(default=None, alias="userId")
    response_time_ms: int = 0
    additional_metadata: Dict[str, Any] = Field(default_factory=dict, alias="additionalMetadata")

from typing import Optional, Dict, Any, List
from pydantic import BaseModel, ConfigDict, Field
from datetime import datetime


def to_camel(string: str) -> str:
    components = string.split('_')
    return components[0] + ''.join(x.title() for x in components[1:])


class BaseSchema(BaseModel):
    model_config = ConfigDict(
        populate_by_name=True,
        alias_generator=to_camel
    )


class RequestFeatures(BaseSchema):
    method: str = Field(default="GET", description="HTTP request method")
    endpoint: str = Field(default="/", description="Target URI endpoint path")
    status_code: int = Field(default=200, description="HTTP response status code", alias="statusCode")
    response_time_ms: int = Field(default=0, description="Response time in milliseconds", alias="responseTimeMs")
    request_size: Optional[int] = Field(default=500, description="Request payload size in bytes", alias="requestSize")
    response_size: Optional[int] = Field(default=1200, description="Response payload size in bytes", alias="responseSize")
    query_params_count: Optional[int] = Field(default=0, description="Count of query parameters", alias="queryParamsCount")


class IdentityFeatures(BaseSchema):
    authenticated: bool = Field(default=False, description="Is request from authenticated user")
    user_id: Optional[str] = Field(default=None, description="Authenticated User ID if available", alias="userId")
    target_account_id: Optional[str] = Field(default=None, description="Target username/account ID", alias="targetAccountId")
    session_id: Optional[str] = Field(default=None, description="Session ID if available", alias="sessionId")


class NetworkFeatures(BaseSchema):
    source_ip: str = Field(default="0.0.0.0", description="Client source IP (or hashed IP)", alias="sourceIp")
    client_ip: Optional[str] = Field(default=None, description="Client source IP alias", alias="clientIp")
    user_agent: Optional[str] = Field(default=None, description="Client User-Agent header", alias="userAgent")


class BehaviorFeatures(BaseSchema):
    # Credential Stuffing Signals
    request_frequency: float = Field(default=0.0, description="Requests per minute/window", alias="requestFrequency")
    failed_request_count: int = Field(default=0, description="Count of failed requests in window", alias="failedRequestCount")
    successful_request_count: int = Field(default=0, description="Count of successful requests", alias="successfulRequestCount")
    failure_ratio: Optional[float] = Field(default=None, description="Failed requests ratio (0.0 - 1.0)", alias="failureRatio")
    unique_users: int = Field(default=1, description="Count of unique target usernames/accounts targeted", alias="uniqueUsers")
    unique_source_ips: int = Field(default=1, description="Count of distinct IPs involved", alias="uniqueSourceIps")
    ip_account_combinations: Optional[int] = Field(default=None, description="Distinct (IP, account) pairs", alias="ipAccountCombinations")
    time_window_seconds: Optional[int] = Field(default=60, description="Observation time window in seconds", alias="timeWindowSeconds")

    # BOLA / ID Enumeration Signals
    unique_object_ids: int = Field(default=0, description="Unique resource/object IDs accessed", alias="uniqueObjectIds")
    objects_accessed_per_user: float = Field(default=0.0, description="Average objects accessed per user", alias="objectsAccessedPerUser")
    sequential_object_access: bool = Field(default=False, description="Flag indicating sequential ID pattern", alias="sequentialObjectAccess")
    object_access_frequency: float = Field(default=0.0, description="Rate of object accesses per minute", alias="objectAccessFrequency")
    different_resources_accessed: int = Field(default=1, description="Distinct resource types accessed", alias="differentResourcesAccessed")
    accessed_object_ids: Optional[List[str]] = Field(default_factory=list, description="Recent object IDs accessed", alias="accessedObjectIds")

    # Authorization context (extensible for Section 11)
    resource_owner_id: Optional[str] = Field(default=None, description="Owner of requested resource", alias="resourceOwnerId")
    requested_object_id: Optional[str] = Field(default=None, description="ID of specific requested object", alias="requestedObjectId")
    authorization_decision: Optional[str] = Field(default=None, description="Authorization check result", alias="authorizationDecision")


class SecurityFeatureSet(BaseSchema):
    event_id: str = Field(..., description="Unique security event ID", alias="eventId")
    application_id: str = Field(..., description="Target application ID", alias="applicationId")
    timestamp: Optional[str] = Field(default=None, description="ISO 8601 Timestamp")

    request_features: RequestFeatures = Field(default_factory=RequestFeatures, alias="requestFeatures")
    identity_features: IdentityFeatures = Field(default_factory=IdentityFeatures, alias="identityFeatures")
    network_features: NetworkFeatures = Field(default_factory=NetworkFeatures, alias="networkFeatures")
    behavior_features: BehaviorFeatures = Field(default_factory=BehaviorFeatures, alias="behaviorFeatures")

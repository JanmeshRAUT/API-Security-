from typing import List
from app.detectors.base_detector import BaseThreatDetector
from app.models.feature_set import SecurityFeatureSet
from app.models.detection_result import DetectionResult, ThreatType, Severity, RecommendedAction
from app.features.feature_processor import FeatureProcessor
from app.ml.threat_model import ThreatModel


class BolaDetector(BaseThreatDetector):
    """
    Detector for Broken Object Level Authorization (BOLA) and ID Enumeration attacks.
    Analyzes object access counts, access frequencies, sequential ID patterns, and resource context.
    Designed with explicit extension hooks for upstream authorization context (Section 11).
    """

    def __init__(self, ml_model: ThreatModel = None):
        self.ml_model = ml_model

    def detect(self, feature_set: SecurityFeatureSet) -> DetectionResult:
        processed = FeatureProcessor.process_bola_features(feature_set)
        reason_codes: List[str] = []
        heuristic_score = 0.0

        unique_objs = processed["unique_object_ids"]
        objs_per_user = processed["objects_accessed_per_user"]
        is_sequential = processed["sequential_object_access"]
        freq = processed["object_access_frequency"]
        diff_resources = processed["different_resources_accessed"]
        auth = processed["authenticated"]

        # Authorization Context hooks (Section 11)
        beh = feature_set.behavior_features
        resource_owner = beh.resource_owner_id
        requested_obj = beh.requested_object_id
        auth_decision = beh.authorization_decision

        # Explicit Authorization Failure context check if provided
        auth_violation = False
        if auth_decision in ["DENIED", "UNAUTHORIZED", "FORBIDDEN"]:
            auth_violation = True
            heuristic_score += 0.40
            reason_codes.append("EXPLICIT_AUTHORIZATION_DENIAL")

        # Heuristic 1: Sequential Object Access
        if is_sequential == 1.0:
            heuristic_score += 0.35
            reason_codes.append("SEQUENTIAL_OBJECT_ACCESS")

        # Heuristic 2: High Unique Object Count
        if unique_objs >= 10:
            heuristic_score += 0.30
            reason_codes.append("HIGH_UNIQUE_OBJECT_COUNT")
        elif unique_objs >= 5:
            heuristic_score += 0.15

        # Heuristic 3: High Object Access Frequency
        if freq >= 15.0:
            heuristic_score += 0.20
            reason_codes.append("HIGH_OBJECT_ACCESS_FREQUENCY")

        # Heuristic 4: Unusual User-to-Object Ratio
        if objs_per_user >= 10.0:
            heuristic_score += 0.20
            reason_codes.append("UNUSUAL_USER_OBJECT_PATTERN")

        # Heuristic 5: Resource Enumeration Pattern
        if (is_sequential == 1.0 or unique_objs >= 8) and freq >= 10.0:
            if "RESOURCE_ENUMERATION_PATTERN" not in reason_codes:
                reason_codes.append("RESOURCE_ENUMERATION_PATTERN")
            heuristic_score += 0.15

        heuristic_score = min(1.0, heuristic_score)

        # ML Anomaly Scoring
        ml_score = 0.0
        if self.ml_model and getattr(self.ml_model, "is_trained", False):
            try:
                vector = FeatureProcessor.to_bola_vector(processed)
                # Check if legacy model expects 6 features (omitting status_code)
                if hasattr(self.ml_model, "model") and hasattr(self.ml_model.model, "n_features_in_"):
                    if self.ml_model.model.n_features_in_ == 6:
                        vector = vector[:6]
                ml_score = float(self.ml_model.score(vector)[0])
            except Exception:
                ml_score = 0.0

        # Combined Risk Score calculation
        if self.ml_model and getattr(self.ml_model, "is_trained", False):
            combined_risk = 0.60 * heuristic_score + 0.40 * ml_score
        else:
            combined_risk = heuristic_score

        combined_risk = max(0.0, min(1.0, combined_risk))

        # Confidence Calculation
        # Distinguish between pure behavioral enumeration vs verified authorization failure.
        # If explicit authorization failure is available, confidence is very high.
        if auth_violation:
            confidence = 0.95
        elif self.ml_model and self.ml_model.is_trained:
            agreement = 1.0 - abs(heuristic_score - ml_score)
            confidence = round(0.70 + 0.25 * agreement, 2)
        else:
            confidence = 0.85 if len(reason_codes) > 0 else 0.90

        confidence = max(0.50, min(0.99, confidence))

        # Determine Threat Type (ID_ENUMERATION vs BOLA)
        # BOLA implies unauthorized resource access attempt; ID_ENUMERATION is resource scanning
        detected = combined_risk >= 0.45 or auth_violation
        if detected:
            if auth_violation or (unique_objs >= 5 and objs_per_user >= 5):
                threat_type = ThreatType.BOLA
            else:
                threat_type = ThreatType.ID_ENUMERATION
        else:
            threat_type = ThreatType.NONE

        # Severity Mapping
        if combined_risk >= 0.80:
            severity = Severity.CRITICAL
            recommended_action = RecommendedAction.BLOCK
        elif combined_risk >= 0.60:
            severity = Severity.HIGH
            recommended_action = RecommendedAction.ALERT if combined_risk < 0.75 else RecommendedAction.BLOCK
        elif combined_risk >= 0.30:
            severity = Severity.MEDIUM
            recommended_action = RecommendedAction.ALERT
        else:
            severity = Severity.LOW
            recommended_action = RecommendedAction.ALLOW

        return DetectionResult(
            eventId=feature_set.event_id,
            detected=detected,
            threatType=threat_type,
            confidence=confidence,
            riskScore=round(combined_risk, 2),
            severity=severity,
            recommendedAction=recommended_action,
            reasonCodes=reason_codes,
            details={
                "heuristicScore": round(heuristic_score, 2),
                "mlAnomalyScore": round(ml_score, 2),
                "processedFeatures": processed,
                "authorizationContext": {
                    "resourceOwnerId": resource_owner,
                    "requestedObjectId": requested_obj,
                    "authorizationDecision": auth_decision
                }
            }
        )

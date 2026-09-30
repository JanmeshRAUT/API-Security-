from typing import List
from app.detectors.base_detector import BaseThreatDetector
from app.models.feature_set import SecurityFeatureSet
from app.models.detection_result import DetectionResult, ThreatType, Severity, RecommendedAction
from app.features.feature_processor import FeatureProcessor
from app.ml.threat_model import ThreatModel


class CredentialStuffingDetector(BaseThreatDetector):
    """
    Hybrid Credential Stuffing & Low-and-Slow Login Abuse Detector.
    Combines unsupervised ML anomaly scores with explicit security heuristics.
    """

    def __init__(self, ml_model: ThreatModel = None):
        self.ml_model = ml_model

    def detect(self, feature_set: SecurityFeatureSet) -> DetectionResult:
        processed = FeatureProcessor.process_credential_stuffing_features(feature_set)
        reason_codes: List[str] = []
        heuristic_score = 0.0

        # Extract features
        freq = processed["request_frequency"]
        failed = processed["failed_request_count"]
        fail_ratio = processed["failure_ratio"]
        users = processed["unique_users"]
        ips = processed["unique_source_ips"]
        ip_combos = processed["ip_account_combinations"]
        is_login = processed["is_login_endpoint"]

        # If not a login endpoint and no failed logins, skip credential stuffing
        if is_login == 0.0 and fail_ratio == 0.0 and freq < 10:
            return DetectionResult(
                eventId=feature_set.event_id,
                detected=False,
                threatType=ThreatType.NONE,
                confidence=0.95,
                riskScore=0.05,
                severity=Severity.LOW,
                recommendedAction=RecommendedAction.ALLOW,
                reasonCodes=[],
                details={"detector": "CredentialStuffingDetector", "status": "non_login_endpoint"}
            )

        # Heuristic 1: High Failure Ratio
        if fail_ratio >= 0.70 and failed >= 3:
            heuristic_score += 0.35
            reason_codes.append("HIGH_FAILURE_RATIO")

        # Heuristic 2: High Request Frequency
        if freq >= 15.0:
            heuristic_score += 0.25
            reason_codes.append("HIGH_LOGIN_FREQUENCY")

        # Heuristic 3: Target Account Spread (Multiple Usernames)
        if users >= 3:
            heuristic_score += 0.20
            reason_codes.append("MULTIPLE_TARGET_ACCOUNTS")

        # Heuristic 4: Source IP Distribution (Distributed Attack)
        if ips >= 3:
            heuristic_score += 0.15
            reason_codes.append("MULTIPLE_SOURCE_IPS")
        if ips >= 2 and users >= 3:
            reason_codes.append("DISTRIBUTED_LOGIN_PATTERN")

        # Heuristic 5: Low-and-Slow Attack Pattern
        # Characteristics: Frequency is modest (< 10 req/min), but failure ratio > 80%, targeting > 3 accounts across >= 2 IPs
        if freq < 10.0 and fail_ratio >= 0.75 and users >= 3 and ips >= 2:
            heuristic_score += 0.30
            if "LOW_AND_SLOW_PATTERN" not in reason_codes:
                reason_codes.append("LOW_AND_SLOW_PATTERN")

        heuristic_score = min(1.0, heuristic_score)

        # ML Anomaly Model Scoring
        ml_score = 0.0
        if self.ml_model and self.ml_model.is_trained:
            vector = FeatureProcessor.to_credential_stuffing_vector(processed)
            ml_score = float(self.ml_model.score(vector)[0])

        # Combined Risk Score (Weighted blend of Heuristic & ML)
        if self.ml_model and self.ml_model.is_trained:
            combined_risk = 0.55 * heuristic_score + 0.45 * ml_score
        else:
            combined_risk = heuristic_score

        combined_risk = max(0.0, min(1.0, combined_risk))

        # Confidence Calculation
        # Confidence reflects how consistent the evidence is.
        # High agreement between ML score and heuristic score yields high confidence.
        if self.ml_model and self.ml_model.is_trained:
            agreement = 1.0 - abs(heuristic_score - ml_score)
            confidence = round(0.70 + 0.25 * agreement, 2)
        else:
            confidence = 0.85 if len(reason_codes) > 0 else 0.90

        confidence = max(0.50, min(0.99, confidence))

        # Threat Detection & Action Thresholds
        detected = combined_risk >= 0.50 or (fail_ratio >= 0.80 and users >= 5)

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
            threatType=ThreatType.CREDENTIAL_STUFFING if detected else ThreatType.NONE,
            confidence=confidence,
            riskScore=round(combined_risk, 2),
            severity=severity,
            recommendedAction=recommended_action,
            reasonCodes=reason_codes,
            details={
                "heuristicScore": round(heuristic_score, 2),
                "mlAnomalyScore": round(ml_score, 2),
                "processedFeatures": processed
            }
        )

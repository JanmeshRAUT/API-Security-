from app.detectors.base_detector import BaseThreatDetector
from app.models.feature_set import SecurityFeatureSet
from app.models.detection_result import DetectionResult, ThreatType, Severity, RecommendedAction
from app.features.feature_processor import FeatureProcessor
from app.ml.model_manager import model_manager

class BehavioralAnomalyDetector(BaseThreatDetector):
    """
    General Behavioral Anomaly Detector executing the experimentally trained 50-feature Isolation Forest.
    """

    def detect(self, feature_set: SecurityFeatureSet) -> DetectionResult:
        # Extract 50-feature DataFrame
        df_features = FeatureProcessor.to_50_feature_dataframe(feature_set)

        # Run inference
        res = model_manager.predict(df_features)

        is_anomaly = res["is_anomaly"]
        raw_score = res["raw_decision_score"]
        norm_score = res["anomaly_score"]

        # Map anomaly severity
        if norm_score >= 0.85:
            severity = Severity.CRITICAL
            action = RecommendedAction.BLOCK
        elif norm_score >= 0.65:
            severity = Severity.HIGH
            action = RecommendedAction.ALERT
        elif norm_score >= 0.50:
            severity = Severity.MEDIUM
            action = RecommendedAction.ALERT
        else:
            severity = Severity.LOW
            action = RecommendedAction.ALLOW

        reason_codes = []
        if is_anomaly:
            reason_codes.append("ANOMALOUS_BEHAVIOR_DETECTED")
            if feature_set.request_features.status_code in [401, 403]:
                reason_codes.append("SUSPICIOUS_HTTP_STATUS")

        return DetectionResult(
            eventId=feature_set.event_id,
            detected=is_anomaly,
            threatType=ThreatType.SUSPICIOUS_BEHAVIOR if is_anomaly else ThreatType.NONE,
            confidence=0.90,
            riskScore=round(norm_score, 2),
            severity=severity,
            recommendedAction=action,
            reasonCodes=reason_codes,
            details={
                "anomalyScore": raw_score,
                "normalizedAnomalyScore": norm_score,
                "threshold": res["threshold"],
                "modelVersion": res["model_version"],
                "featureVersion": res["feature_version"]
            }
        )

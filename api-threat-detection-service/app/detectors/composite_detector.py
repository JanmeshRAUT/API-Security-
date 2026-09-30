from typing import List
from app.detectors.base_detector import BaseThreatDetector
from app.models.feature_set import SecurityFeatureSet
from app.models.detection_result import DetectionResult, ThreatType, Severity, RecommendedAction


class CompositeDetector(BaseThreatDetector):
    """
    Composite Detector that orchestrates individual threat detectors
    (Credential Stuffing, BOLA / ID Enumeration) and synthesizes the final DetectionResult.
    """

    def __init__(self, detectors: List[BaseThreatDetector]):
        self.detectors = detectors

    def detect(self, feature_set: SecurityFeatureSet) -> DetectionResult:
        results: List[DetectionResult] = []
        for detector in self.detectors:
            res = detector.detect(feature_set)
            results.append(res)

        # Filter detected threats
        detected_results = [r for r in results if r.detected]

        if not detected_results:
            # If no threat detected, select result with maximum risk score or default to NONE
            max_res = max(results, key=lambda r: r.risk_score) if results else None
            if max_res:
                return max_res
            return DetectionResult(
                eventId=feature_set.event_id,
                detected=False,
                threatType=ThreatType.NONE,
                confidence=0.95,
                riskScore=0.0,
                severity=Severity.LOW,
                recommendedAction=RecommendedAction.ALLOW,
                reasonCodes=[]
            )

        # Sort detected results by highest risk score then highest confidence
        detected_results.sort(key=lambda r: (r.risk_score, r.confidence), reverse=True)
        primary_threat = detected_results[0]

        # Aggregate reason codes across all triggered detectors
        all_reason_codes = []
        for r in detected_results:
            for rc in r.reason_codes:
                if rc not in all_reason_codes:
                    all_reason_codes.append(rc)

        # Synthesize combined details
        aggregated_details = {
            "primaryDetectorResult": primary_threat.details,
            "allEvaluatedThreats": [
                {
                    "threatType": r.threat_type.value,
                    "riskScore": r.risk_score,
                    "confidence": r.confidence,
                    "severity": r.severity.value,
                    "reasonCodes": r.reason_codes
                }
                for r in results
            ]
        }

        return DetectionResult(
            eventId=feature_set.event_id,
            detected=True,
            threatType=primary_threat.threat_type,
            confidence=primary_threat.confidence,
            riskScore=primary_threat.risk_score,
            severity=primary_threat.severity,
            recommendedAction=primary_threat.recommended_action,
            reasonCodes=all_reason_codes,
            details=aggregated_details
        )

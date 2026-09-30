import logging
from app.models.feature_set import SecurityFeatureSet
from app.models.detection_result import DetectionResult
from app.detectors.behavioral_anomaly_detector import BehavioralAnomalyDetector
from app.detectors.credential_stuffing_detector import CredentialStuffingDetector
from app.detectors.bola_detector import BolaDetector
from app.detectors.composite_detector import CompositeDetector

logger = logging.getLogger("detection_service")

class DetectionService:
    """
    Core application service layer for threat detection.
    Initializes hybrid detectors (trained Isolation Forest behavioral anomaly + specialized credential stuffing & BOLA detectors).
    """

    def __init__(self):
        # Initialize detectors
        self.behavioral_detector = BehavioralAnomalyDetector()
        self.cs_detector = CredentialStuffingDetector()
        self.bola_detector = BolaDetector()

        # Initialize composite detector orchestrator
        self.composite_detector = CompositeDetector(detectors=[
            self.behavioral_detector,
            self.cs_detector,
            self.bola_detector
        ])

    def evaluate(self, feature_set: SecurityFeatureSet) -> DetectionResult:
        """
        Evaluates incoming SecurityFeatureSet against all active detectors and returns DetectionResult.
        """
        logger.debug(f"Evaluating security event {feature_set.event_id} for app {feature_set.application_id}")
        return self.composite_detector.detect(feature_set)

from abc import ABC, abstractmethod
from app.models.feature_set import SecurityFeatureSet
from app.models.detection_result import DetectionResult


class BaseThreatDetector(ABC):
    """
    Abstract base interface for threat detectors.
    Defines contract: SecurityFeatureSet -> Detector -> DetectionResult
    """

    @abstractmethod
    def detect(self, feature_set: SecurityFeatureSet) -> DetectionResult:
        """
        Analyzes feature_set and returns standardized DetectionResult.
        """
        pass

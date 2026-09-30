from abc import ABC, abstractmethod
import numpy as np
from typing import Any, Dict


class ThreatModel(ABC):
    """
    Abstract interface for machine learning threat detection models.
    Decouples threat detectors from specific ML framework implementations.
    """

    @abstractmethod
    def train(self, X: np.ndarray, y: np.ndarray = None) -> None:
        """
        Train or fit the model on feature matrix X.
        """
        pass

    @abstractmethod
    def predict(self, X: np.ndarray) -> np.ndarray:
        """
        Predict binary labels or anomaly indications (-1 for anomaly/threat, 1 for normal).
        """
        pass

    @abstractmethod
    def score(self, X: np.ndarray) -> np.ndarray:
        """
        Compute anomaly score normalized between 0.0 (normal) and 1.0 (highly anomalous).
        """
        pass

    @abstractmethod
    def save(self, filepath: str) -> None:
        """
        Persist trained model weights to disk.
        """
        pass

    @abstractmethod
    def load(self, filepath: str) -> None:
        """
        Load trained model weights from disk.
        """
        pass

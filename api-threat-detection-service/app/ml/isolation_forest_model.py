import os
import joblib
import numpy as np
from sklearn.ensemble import IsolationForest
from app.ml.threat_model import ThreatModel


class IsolationForestModel(ThreatModel):
    """
    Unsupervised Anomaly Detection using Scikit-Learn Isolation Forest.
    Normalizes decision function scores to range [0.0, 1.0] where 1.0 indicates high anomaly.
    """

    def __init__(self, n_estimators: int = 100, contamination: float = 0.1, random_state: int = 42):
        self.n_estimators = n_estimators
        self.contamination = contamination
        self.random_state = random_state
        self.model = IsolationForest(
            n_estimators=self.n_estimators,
            contamination=self.contamination,
            random_state=self.random_state
        )
        self.is_trained = False

    def train(self, X: np.ndarray, y: np.ndarray = None) -> None:
        """
        Fit IsolationForest model on matrix X.
        """
        self.model.fit(X)
        self.is_trained = True

    def predict(self, X: np.ndarray) -> np.ndarray:
        """
        Returns predictions: -1 for anomaly, 1 for normal.
        """
        if not self.is_trained:
            raise RuntimeError("Model must be trained before predicting.")
        if X.ndim == 1:
            X = X.reshape(1, -1)
        return self.model.predict(X)

    def score(self, X: np.ndarray) -> np.ndarray:
        """
        Returns normalized anomaly score between 0.0 (very normal) and 1.0 (highly anomalous).
        IsolationForest decision_function returns negative values for anomalies and positive for normal.
        """
        if not self.is_trained:
            raise RuntimeError("Model must be trained before scoring.")
        if X.ndim == 1:
            X = X.reshape(1, -1)

        # raw score: lower means more anomalous (e.g. -0.5 is very anomalous, +0.3 is normal)
        raw_scores = self.model.decision_function(X)

        # Map decision_function values [-0.5, 0.5] to anomaly score [1.0, 0.0]
        # score = 0.5 - raw_score, then clipped to [0, 1]
        anomaly_scores = 0.5 - raw_scores
        anomaly_scores = np.clip(anomaly_scores, 0.0, 1.0)
        return anomaly_scores

    def save(self, filepath: str) -> None:
        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        joblib.dump(self.model, filepath)

    def load(self, filepath: str) -> None:
        if os.path.exists(filepath):
            self.model = joblib.load(filepath)
            self.is_trained = True
        else:
            raise FileNotFoundError(f"Model file not found at {filepath}")

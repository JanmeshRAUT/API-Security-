import os
import sys
import json
import joblib
import numpy as np
import pandas as pd
from typing import Dict, Any

# Ensure api-security-evaluation is in sys.path so joblib can locate src.training.preprocessing.FeaturePreprocessor
eval_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "..", "api-security-evaluation"))
if eval_dir not in sys.path and os.path.exists(eval_dir):
    sys.path.insert(0, eval_dir)

# Fallback alias if src is loaded under evaluation path
try:
    import src.training.preprocessing
except ImportError:
    import types
    src_mod = types.ModuleType("src")
    sys.modules["src"] = src_mod
    tr_mod = types.ModuleType("src.training")
    sys.modules["src.training"] = tr_mod
    src_mod.training = tr_mod
    pr_mod = types.ModuleType("src.training.preprocessing")
    sys.modules["src.training.preprocessing"] = pr_mod
    tr_mod.preprocessing = pr_mod

    class FeaturePreprocessor:
        def __init__(self, *args, **kwargs):
            self.numeric_cols = []
            self.categorical_cols = []
            self.encoder = None
            self.scaler = None

        def transform(self, df):
            df_copy = df.copy()
            num_cols = [c for c in self.numeric_cols if c in df_copy.columns]
            cat_cols = [c for c in self.categorical_cols if c in df_copy.columns]
            
            if hasattr(self, "encoder") and self.encoder is not None and cat_cols:
                df_copy[cat_cols] = self.encoder.transform(df_copy[cat_cols])
            
            if hasattr(self, "scaler") and self.scaler is not None and num_cols:
                df_copy[num_cols] = self.scaler.transform(df_copy[num_cols])
            
            all_cols = num_cols + cat_cols
            return df_copy[all_cols]

        def fit(self, df):
            return self

        def fit_transform(self, df):
            return self.transform(df)

    pr_mod.FeaturePreprocessor = FeaturePreprocessor

from app.config.settings import settings
from app.features.feature_schema import FEATURE_VERSION, ML_FEATURE_COLUMNS

class ModelManager:
    """
    Singleton Manager for loading and managing the persisted 50-feature Isolation Forest model
    and preprocessor bundle trained in api-security-evaluation.
    """

    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(ModelManager, cls).__new__(cls)
            cls._instance._is_initialized = False
        return cls._instance

    def __init__(self):
        if self._is_initialized:
            return

        self.model = None
        self.preprocessor = None
        self.metadata: Dict[str, Any] = {}
        self.is_loaded = False
        self.threshold = settings.model_threshold
        self._is_initialized = True
        if os.path.exists(settings.model_path):
            try:
                self.load_model()
            except Exception:
                pass

    def load_model(self, model_path: str = None, metadata_path: str = None) -> None:
        path = model_path or settings.model_path
        meta_path = metadata_path or settings.metadata_path

        if not os.path.exists(path):
            raise FileNotFoundError(f"Trained Isolation Forest model file not found at: '{path}'")

        # 1. Load joblib bundle (contains model and preprocessor)
        bundle = joblib.load(path)
        if not isinstance(bundle, dict) or "model" not in bundle or "preprocessor" not in bundle:
            raise ValueError(f"Invalid model bundle structure in '{path}'. Expected dict with 'model' and 'preprocessor'.")

        self.model = bundle["model"]
        self.preprocessor = bundle["preprocessor"]

        # 2. Load metadata if present
        if os.path.exists(meta_path):
            with open(meta_path, "r", encoding="utf-8") as f:
                self.metadata = json.load(f)

        # 3. Feature compatibility validation
        prep_cols = set(self.preprocessor.numeric_cols + self.preprocessor.categorical_cols)
        schema_cols = set(ML_FEATURE_COLUMNS)
        # Check if preprocessor features are a subset or exact match with schema columns
        if not prep_cols.issubset(schema_cols) and prep_cols != schema_cols:
            raise ValueError(
                f"Feature compatibility error! Preprocessor columns ({len(prep_cols)}) "
                f"are not compatible with canonical schema contract '{FEATURE_VERSION}' ({len(schema_cols)})."
            )

        self.is_loaded = True

    def predict(self, df_features: pd.DataFrame) -> Dict[str, Any]:
        """
        Performs inference using persisted preprocessor and model.
        Returns raw decision_function score, normalized anomaly score, and boolean anomaly decision.
        """
        if not self.is_loaded:
            self.load_model()

        # Apply exact preprocessing
        X_proc = self.preprocessor.transform(df_features)

        # Raw decision_function score (negative = anomalous, positive = normal)
        raw_score = float(self.model.decision_function(X_proc)[0])
        is_anomaly = bool(raw_score < self.threshold)

        # Map raw_score to normalized 0.0 -> 1.0 anomaly score
        normalized_anomaly_score = float(np.clip(0.5 - raw_score, 0.0, 1.0))

        return {
            "raw_decision_score": raw_score,
            "anomaly_score": normalized_anomaly_score,
            "is_anomaly": is_anomaly,
            "threshold": self.threshold,
            "model_version": self.metadata.get("model_version", "v003"),
            "feature_version": FEATURE_VERSION,
        }

model_manager = ModelManager()

from pydantic_settings import BaseSettings
from pydantic import Field

class Settings(BaseSettings):
    app_name: str = "AI/ML API Threat Detection Service"
    version: str = "1.0.0"
    host: str = "0.0.0.0"
    port: int = 8000
    log_level: str = "INFO"

    # Persisted Trained Model Settings
    model_enabled: bool = True
    model_path: str = Field("models/isolation_forest/isolation_forest_v003.joblib", description="Path to trained model bundle")
    metadata_path: str = Field("models/isolation_forest/metadata_v003.json", description="Path to model metadata")
    model_threshold: float = Field(-0.05, description="Decision function threshold (-0.05 optimal F1 operating balance)")

    # Risk thresholds
    risk_threshold_low: float = Field(0.30, description="Threshold for LOW -> MEDIUM risk")
    risk_threshold_medium: float = Field(0.60, description="Threshold for MEDIUM -> HIGH risk")
    risk_threshold_high: float = Field(0.80, description="Threshold for HIGH -> CRITICAL risk")

    # Action thresholds
    action_alert_threshold: float = Field(0.50, description="Risk score threshold to trigger ALERT")
    action_block_threshold: float = Field(0.80, description="Risk score threshold to trigger BLOCK")

    class Config:
        env_file = ".env"
        extra = "ignore"

settings = Settings()

import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.config.settings import settings
from app.api.routes.detection import router as detection_router
from app.ml.model_manager import model_manager

logging.basicConfig(
    level=settings.log_level,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("main")

@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Application lifespan context manager. Pre-loads and validates persisted trained ML models.
    """
    logger.info(f"Starting {settings.app_name} v{settings.version}")
    logger.info("Initializing and validating trained 50-feature Isolation Forest model...")
    try:
        model_manager.load_model()
        logger.info(f"Persisted Isolation Forest model loaded successfully. (Version: {model_manager.metadata.get('model_version')}, Threshold: {model_manager.threshold})")
    except Exception as e:
        logger.error(f"Failed to load trained model: {e}")

    yield
    logger.info("Shutting down AI/ML API Threat Detection Service.")

app = FastAPI(
    title=settings.app_name,
    version=settings.version,
    description="Central AI/ML API Threat Detection Service providing real-time behavioral and anomaly threat detection.",
    lifespan=lifespan
)

app.include_router(detection_router)

@app.get("/health")
def health_check():
    return {
        "status": "UP" if model_manager.is_loaded else "degraded",
        "app": settings.app_name,
        "version": settings.version,
        "modelLoaded": model_manager.is_loaded,
        "modelVersion": model_manager.metadata.get("model_version", "v003"),
        "featureVersion": "feature_v002",
        "threshold": model_manager.threshold,
        "credentialStuffingModel": "LOADED" if model_manager.is_loaded else "NOT_LOADED",
        "bolaModel": "LOADED" if model_manager.is_loaded else "NOT_LOADED",
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host=settings.host, port=settings.port, reload=True)

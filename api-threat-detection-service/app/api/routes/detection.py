from fastapi import APIRouter, HTTPException, Depends, status
from app.models.feature_set import SecurityFeatureSet
from app.models.detection_result import DetectionResult
from app.services.detection_service import DetectionService
from app.ml.model_manager import model_manager

router = APIRouter()

# Global service reference initialized at app startup
_detection_service: DetectionService = None


def get_detection_service() -> DetectionService:
    global _detection_service
    if _detection_service is None:
        _detection_service = DetectionService()
    return _detection_service


@router.post("/api/v1/detect", response_model=DetectionResult, status_code=status.HTTP_200_OK)
async def detect_threat(
    feature_set: SecurityFeatureSet,
    service: DetectionService = Depends(get_detection_service)
) -> DetectionResult:
    """
    Evaluates incoming SecurityFeatureSet event for security threats (Credential Stuffing, BOLA/ID Enumeration).
    Returns standardized DetectionResult with risk score, confidence, severity, and explainable reason codes.
    """
    try:
        result = service.evaluate(feature_set)
        return result
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"An error occurred during threat evaluation: {str(e)}"
        )


@router.get("/health", status_code=status.HTTP_200_OK)
async def health_check():
    """
    Health check endpoint returning service operational status and model readiness.
    """
    return {
        "status": "healthy" if model_manager.is_loaded else "degraded",
        "app": "AI/ML API Threat Detection Service",
        "modelLoaded": model_manager.is_loaded,
        "modelVersion": model_manager.metadata.get("model_version", "v003"),
        "featureVersion": "feature_v002",
        "threshold": model_manager.threshold,
        "credentialStuffingModel": "LOADED" if model_manager.is_loaded else "NOT_LOADED",
        "bolaModel": "LOADED" if model_manager.is_loaded else "NOT_LOADED",
    }

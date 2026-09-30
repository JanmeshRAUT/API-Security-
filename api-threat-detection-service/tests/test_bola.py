import pytest
from app.models.feature_set import (
    SecurityFeatureSet, RequestFeatures, IdentityFeatures, NetworkFeatures, BehaviorFeatures
)
from app.detectors.bola_detector import BolaDetector
from app.models.detection_result import ThreatType, Severity, RecommendedAction
from app.ml.isolation_forest_model import IsolationForestModel
from app.ml.synthetic_data import generate_bola_data


@pytest.fixture(scope="module")
def trained_bola_model():
    model = IsolationForestModel(n_estimators=50, contamination=0.15, random_state=42)
    X, _ = generate_bola_data(n_samples=300)
    model.train(X)
    return model


@pytest.fixture
def detector(trained_bola_model):
    return BolaDetector(ml_model=trained_bola_model)


def test_normal_object_access(detector):
    feature_set = SecurityFeatureSet(
        eventId="evt-normal-bola",
        applicationId="shop-sphere",
        requestFeatures=RequestFeatures(method="GET", endpoint="/api/orders/101", statusCode=200),
        identityFeatures=IdentityFeatures(authenticated=True, userId="user-123"),
        behaviorFeatures=BehaviorFeatures(
            uniqueObjectIds=1,
            objectsAccessedPerUser=1.0,
            sequentialObjectAccess=False,
            objectAccessFrequency=2.0,
            accessedObjectIds=["101", "101", "101"]
        )
    )
    res = detector.detect(feature_set)
    assert not res.detected
    assert res.threat_type == ThreatType.NONE
    assert res.risk_score < 0.35
    assert res.recommended_action == RecommendedAction.ALLOW


def test_sequential_object_id_enumeration(detector):
    feature_set = SecurityFeatureSet(
        eventId="evt-seq-enum",
        applicationId="shop-sphere",
        requestFeatures=RequestFeatures(method="GET", endpoint="/api/orders/", statusCode=200),
        identityFeatures=IdentityFeatures(authenticated=True, userId="user-attacker"),
        behaviorFeatures=BehaviorFeatures(
            uniqueObjectIds=15,
            objectsAccessedPerUser=15.0,
            sequentialObjectAccess=True,
            objectAccessFrequency=25.0,
            accessedObjectIds=["101", "102", "103", "104", "105"]
        )
    )
    res = detector.detect(feature_set)
    assert res.detected
    assert res.threat_type in [ThreatType.BOLA, ThreatType.ID_ENUMERATION]
    assert "SEQUENTIAL_OBJECT_ACCESS" in res.reason_codes
    assert "HIGH_UNIQUE_OBJECT_COUNT" in res.reason_codes
    assert res.risk_score >= 0.60


def test_high_unique_object_access(detector):
    feature_set = SecurityFeatureSet(
        eventId="evt-high-unique",
        applicationId="shop-sphere",
        requestFeatures=RequestFeatures(method="GET", endpoint="/api/documents/", statusCode=200),
        behaviorFeatures=BehaviorFeatures(
            uniqueObjectIds=30,
            objectsAccessedPerUser=30.0,
            sequentialObjectAccess=False,
            objectAccessFrequency=40.0
        )
    )
    res = detector.detect(feature_set)
    assert res.detected
    assert "HIGH_UNIQUE_OBJECT_COUNT" in res.reason_codes
    assert "HIGH_OBJECT_ACCESS_FREQUENCY" in res.reason_codes


def test_explicit_authorization_denial(detector):
    feature_set = SecurityFeatureSet(
        eventId="evt-auth-denied",
        applicationId="shop-sphere",
        requestFeatures=RequestFeatures(method="GET", endpoint="/api/accounts/999", statusCode=403),
        behaviorFeatures=BehaviorFeatures(
            uniqueObjectIds=5,
            requestedObjectId="999",
            authorizationDecision="DENIED"
        )
    )
    res = detector.detect(feature_set)
    assert res.detected
    assert "EXPLICIT_AUTHORIZATION_DENIAL" in res.reason_codes
    assert res.confidence >= 0.90

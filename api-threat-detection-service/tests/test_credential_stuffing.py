import pytest
from app.models.feature_set import (
    SecurityFeatureSet, RequestFeatures, IdentityFeatures, NetworkFeatures, BehaviorFeatures
)
from app.detectors.credential_stuffing_detector import CredentialStuffingDetector
from app.models.detection_result import ThreatType, Severity, RecommendedAction
from app.ml.isolation_forest_model import IsolationForestModel
from app.ml.synthetic_data import generate_credential_stuffing_data


@pytest.fixture(scope="module")
def trained_cs_model():
    model = IsolationForestModel(n_estimators=50, contamination=0.15, random_state=42)
    X, _ = generate_credential_stuffing_data(n_samples=300)
    model.train(X)
    return model


@pytest.fixture
def detector(trained_cs_model):
    return CredentialStuffingDetector(ml_model=trained_cs_model)


def test_normal_login_activity(detector):
    feature_set = SecurityFeatureSet(
        eventId="evt-normal-login",
        applicationId="shop-sphere",
        requestFeatures=RequestFeatures(method="POST", endpoint="/api/auth/login", statusCode=200),
        behaviorFeatures=BehaviorFeatures(
            requestFrequency=2.0,
            failedRequestCount=0,
            successfulRequestCount=5,
            failureRatio=0.0,
            uniqueUsers=1,
            uniqueSourceIps=1
        )
    )
    res = detector.detect(feature_set)
    assert not res.detected
    assert res.threat_type == ThreatType.NONE
    assert res.risk_score < 0.30
    assert res.severity == Severity.LOW
    assert res.recommended_action == RecommendedAction.ALLOW


def test_high_failure_ratio_credential_stuffing(detector):
    feature_set = SecurityFeatureSet(
        eventId="evt-high-fail",
        applicationId="shop-sphere",
        requestFeatures=RequestFeatures(method="POST", endpoint="/api/auth/login", statusCode=401),
        behaviorFeatures=BehaviorFeatures(
            requestFrequency=50.0,
            failedRequestCount=45,
            successfulRequestCount=5,
            failureRatio=0.90,
            uniqueUsers=25,
            uniqueSourceIps=10
        )
    )
    res = detector.detect(feature_set)
    assert res.detected
    assert res.threat_type == ThreatType.CREDENTIAL_STUFFING
    assert res.risk_score >= 0.60
    assert "HIGH_FAILURE_RATIO" in res.reason_codes
    assert "HIGH_LOGIN_FREQUENCY" in res.reason_codes
    assert "MULTIPLE_TARGET_ACCOUNTS" in res.reason_codes
    assert "MULTIPLE_SOURCE_IPS" in res.reason_codes


def test_distributed_login_behavior(detector):
    feature_set = SecurityFeatureSet(
        eventId="evt-distributed",
        applicationId="shop-sphere",
        requestFeatures=RequestFeatures(method="POST", endpoint="/api/v1/login", statusCode=401),
        behaviorFeatures=BehaviorFeatures(
            requestFrequency=30.0,
            failedRequestCount=25,
            successfulRequestCount=2,
            failureRatio=0.92,
            uniqueUsers=15,
            uniqueSourceIps=12
        )
    )
    res = detector.detect(feature_set)
    assert res.detected
    assert res.threat_type == ThreatType.CREDENTIAL_STUFFING
    assert "DISTRIBUTED_LOGIN_PATTERN" in res.reason_codes


def test_low_and_slow_credential_stuffing(detector):
    """
    Low-and-Slow Attack Case:
    Individual request frequency is low (e.g. 4 req/min), but targets 8 accounts across 5 IPs with 85% failure ratio.
    """
    feature_set = SecurityFeatureSet(
        eventId="evt-low-slow",
        applicationId="shop-sphere",
        requestFeatures=RequestFeatures(method="POST", endpoint="/api/auth/signin", statusCode=401),
        behaviorFeatures=BehaviorFeatures(
            requestFrequency=4.0,
            failedRequestCount=7,
            successfulRequestCount=1,
            failureRatio=0.875,
            uniqueUsers=8,
            uniqueSourceIps=5
        )
    )
    res = detector.detect(feature_set)
    assert res.detected
    assert res.threat_type == ThreatType.CREDENTIAL_STUFFING
    assert "LOW_AND_SLOW_PATTERN" in res.reason_codes
    assert res.risk_score >= 0.50

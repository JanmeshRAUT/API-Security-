from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] in ["UP", "healthy"]
    assert data["modelLoaded"] is True
    assert "credentialStuffingModel" in data
    assert "bolaModel" in data


def test_detect_endpoint_valid_credential_stuffing():
    payload = {
        "eventId": "event-123",
        "applicationId": "shop-sphere",
        "timestamp": "2026-09-02T12:30:00Z",
        "requestFeatures": {
            "method": "POST",
            "endpoint": "/api/auth/login",
            "statusCode": 401,
            "responseTimeMs": 120
        },
        "identityFeatures": {
            "authenticated": False
        },
        "networkFeatures": {
            "sourceIp": "192.168.1.100"
        },
        "behaviorFeatures": {
            "requestFrequency": 45,
            "failedRequestCount": 40,
            "successfulRequestCount": 5,
            "failureRatio": 0.88,
            "uniqueUsers": 20,
            "uniqueSourceIps": 8
        }
    }
    response = client.post("/api/v1/detect", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["eventId"] == "event-123"
    assert data["detected"] is True
    assert data["threatType"] == "CREDENTIAL_STUFFING"
    assert 0.0 <= data["confidence"] <= 1.0
    assert 0.0 <= data["riskScore"] <= 1.0
    assert data["severity"] in ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
    assert data["recommendedAction"] in ["ALLOW", "ALERT", "BLOCK"]
    assert isinstance(data["reasonCodes"], list)
    assert len(data["reasonCodes"]) > 0


def test_detect_endpoint_valid_bola():
    payload = {
        "eventId": "event-456",
        "applicationId": "shop-sphere",
        "timestamp": "2026-09-02T12:35:00Z",
        "requestFeatures": {
            "method": "GET",
            "endpoint": "/api/orders/105",
            "statusCode": 200,
            "responseTimeMs": 50
        },
        "identityFeatures": {
            "authenticated": True,
            "userId": "user-attacker"
        },
        "networkFeatures": {
            "sourceIp": "10.0.0.1"
        },
        "behaviorFeatures": {
            "uniqueObjectIds": 25,
            "objectsAccessedPerUser": 25.0,
            "sequentialObjectAccess": True,
            "objectAccessFrequency": 35.0,
            "accessedObjectIds": ["101", "102", "103", "104", "105"]
        }
    }
    response = client.post("/api/v1/detect", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["eventId"] == "event-456"
    assert data["detected"] is True
    assert data["threatType"] in ["BOLA", "ID_ENUMERATION"]
    assert "SEQUENTIAL_OBJECT_ACCESS" in data["reasonCodes"]


def test_detect_endpoint_invalid_payload():
    # Missing required field eventId and applicationId
    payload = {
        "requestFeatures": {
            "method": "POST"
        }
    }
    response = client.post("/api/v1/detect", json=payload)
    assert response.status_code == 422  # Validation Error

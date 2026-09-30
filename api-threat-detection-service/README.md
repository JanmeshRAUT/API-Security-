# Central AI/ML API Threat Detection Service

An independent, lightweight Python 3.11+ FastAPI microservice providing real-time behavioral and anomaly-based threat detection for protected web applications.

It receives standard `SecurityFeatureSet` payloads produced by the Spring Boot Security Starter, processes behavioral feature vectors, and evaluates threats using a hybrid model architecture combining **Unsupervised Anomaly Detection (Isolation Forest)** and **Security Heuristics**.

---

## 1. Architecture

```text
                    Protected Application
                           │
                           ▼
                  Spring Boot Starter
                           │
                           ▼
                   Security Events
                           │
                           ▼
                  Feature Extraction
                           │
                           ▼
                  SecurityFeatureSet
                           │
                           ▼
                Central Detection API
                (POST /api/v1/detect)
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
      Credential Stuffing          BOLA Detection
           Detector                   Detector
              │                         │
              └────────────┬────────────┘
                           ▼
                    Threat Assessment
                           │
                           ▼
                    Detection Result
                           │
                           ▼
                 Security Platform
```

### Components
- **API Layer (`app/api/routes/detection.py`)**: Exposes `POST /api/v1/detect` and `GET /health`. Validates input using Pydantic models with camelCase / snake_case aliasing.
- **Models (`app/models/`)**: Pydantic schemas for `SecurityFeatureSet` and standardized `DetectionResult`.
- **Feature Processor (`app/features/feature_processor.py`)**: Normalizes missing fields, computes derived ratios (e.g. failure ratios), and extracts model feature vectors.
- **ML Engine (`app/ml/`)**: Abstract `ThreatModel` interface with `IsolationForestModel` implementation and synthetic data generator. Pre-trained models are kept in memory for fast inference.
- **Detectors (`app/detectors/`)**:
  - `CredentialStuffingDetector`: Detects brute force login attacks, distributed credential stuffing, and low-and-slow login abuse.
  - `BolaDetector`: Detects Broken Object Level Authorization (BOLA) and sequential object ID enumeration.
  - `CompositeDetector`: Orchestrates detectors, selects primary threat, and calculates combined risk score, confidence, severity, and explainability reason codes.

---

## 2. Supported Threats & Reason Codes

### Credential Stuffing & Login Abuse (`CREDENTIAL_STUFFING`)
- `HIGH_FAILURE_RATIO`: Failed login attempts ratio ≥ 70%.
- `HIGH_LOGIN_FREQUENCY`: Rapid login attempts per window.
- `MULTIPLE_TARGET_ACCOUNTS`: Abnormally high number of unique target usernames.
- `MULTIPLE_SOURCE_IPS`: Attack originating across multiple client IPs.
- `DISTRIBUTED_LOGIN_PATTERN`: Multi-account, multi-IP login abuse.
- `LOW_AND_SLOW_PATTERN`: Low request frequency, high failure ratio across multiple IPs and accounts designed to evade rate limiters.

### BOLA & ID Enumeration (`BOLA` / `ID_ENUMERATION`)
- `SEQUENTIAL_OBJECT_ACCESS`: Sequential resource ID access pattern (e.g., `/orders/101`, `/orders/102`).
- `HIGH_UNIQUE_OBJECT_COUNT`: Abnormally high count of distinct object IDs accessed.
- `HIGH_OBJECT_ACCESS_FREQUENCY`: High rate of object accesses per minute.
- `UNUSUAL_USER_OBJECT_PATTERN`: High ratio of distinct objects accessed per user.
- `RESOURCE_ENUMERATION_PATTERN`: Automated scanning of parameterized API resource paths.
- `EXPLICIT_AUTHORIZATION_DENIAL`: Access attempt resulted in explicit HTTP 403/401 authorization failure.

---

## 3. Scoring Methodology

### Risk Score (`riskScore`)
Normalized float between `0.0` (safe) and `1.0` (highly dangerous).
Calculated using a weighted blend of ML anomaly score and heuristic security rule evaluation:
$$\text{RiskScore} = w_{h} \times \text{HeuristicScore} + w_{m} \times \text{MLAnomalyScore}$$

- `0.00 - 0.29`: `LOW` severity → `ALLOW` action
- `0.30 - 0.59`: `MEDIUM` severity → `ALERT` action
- `0.60 - 0.79`: `HIGH` severity → `ALERT` or `BLOCK` action
- `0.80 - 1.00`: `CRITICAL` severity → `BLOCK` action

### Confidence (`confidence`)
Independent metric between `0.50` and `0.99` representing classification certainty based on signal consistency and agreement between the ML model and security heuristics.

---

## 4. Getting Started & Running the Service

### Prerequisites
- Python 3.11+
- `pip`

### Installation
```bash
cd api-threat-detection-service
python -m venv venv
# On Windows:
venv\Scripts\activate
# On Linux/macOS:
source venv/bin/activate

pip install -r requirements.txt
```

### Running the API Service
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
The service will automatically train initial Isolation Forest models on synthetic data if pre-trained joblib weights are not found in `app/ml/artifacts/`.

### Running Tests
```bash
pytest tests/ -v
```

---

## 5. API Usage Examples

### Detection Endpoint (`POST /api/v1/detect`)

#### Request (Credential Stuffing Payload)
```json
{
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
    "authenticated": false
  },
  "networkFeatures": {
    "sourceIp": "192.168.1.1"
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
```

#### Response
```json
{
  "eventId": "event-123",
  "detected": true,
  "threatType": "CREDENTIAL_STUFFING",
  "confidence": 0.94,
  "riskScore": 0.91,
  "severity": "CRITICAL",
  "recommendedAction": "BLOCK",
  "reasonCodes": [
    "HIGH_FAILURE_RATIO",
    "HIGH_LOGIN_FREQUENCY",
    "MULTIPLE_TARGET_ACCOUNTS",
    "MULTIPLE_SOURCE_IPS",
    "DISTRIBUTED_LOGIN_PATTERN"
  ]
}
```

### Health Check Endpoint (`GET /health`)
```json
{
  "status": "UP",
  "credentialStuffingModel": "READY",
  "bolaModel": "READY"
}
```

---

## 6. Research Integrity & Known Limitations

1. **Synthetic Training Data**: The current ML model is trained on controlled synthetic dataset samples. Real-world deployment should retrain models using anonymized production traffic baseline data.
2. **Authorization Ownership Context**: Traffic-level behavioral analysis detects ID enumeration scanning, but cannot guarantee authorization ownership unless upstream services provide `resourceOwnerId` and `authorizationDecision` context.
3. **No Database / State Storage**: The detection service is strictly stateless for fast inference. Event history aggregation is performed by the Spring Boot Starter or security platform layer.

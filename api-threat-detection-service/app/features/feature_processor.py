import numpy as np
import pandas as pd
import time
import hashlib
from typing import Dict, Any, Tuple
from app.models.feature_set import SecurityFeatureSet


class FeatureProcessor:
    """
    Validates feature values, handles missing fields, computes derived signals,
    and converts SecurityFeatureSet into model-ready numeric vectors.
    """

    @staticmethod
    def process_credential_stuffing_features(feature_set: SecurityFeatureSet) -> Dict[str, Any]:
        """
        Extracts and normalizes features relevant to Credential Stuffing.
        Features used:
        - requestFrequency: Number of login attempts per window
        - failedRequestCount: Total failed login requests
        - failureRatio: Computed as failed / total requests (0.0 to 1.0)
        - uniqueUsers: Number of distinct usernames targeted
        - uniqueSourceIps: Number of distinct client IPs involved
        - ipAccountCombinations: Number of distinct (IP, Account) pairs
        - isLoginEndpoint: 1.0 if endpoint is login/auth, else 0.0
        """
        req = feature_set.request_features
        beh = feature_set.behavior_features
        net = feature_set.network_features

        endpoint_lower = req.endpoint.lower()
        is_login = 1.0 if any(kw in endpoint_lower for kw in ["login", "auth", "signin", "token", "password"]) else 0.0

        # Calculate failure ratio if omitted
        failed = max(0, beh.failed_request_count)
        successful = max(0, beh.successful_request_count)
        total_requests = failed + successful

        if beh.failure_ratio is not None:
            failure_ratio = float(np.clip(beh.failure_ratio, 0.0, 1.0))
        elif total_requests > 0:
            failure_ratio = float(failed / total_requests)
        elif req.status_code in [401, 403]:
            failure_ratio = 1.0
        else:
            failure_ratio = 0.0

        unique_users = max(1, beh.unique_users)
        unique_ips = max(1, beh.unique_source_ips)
        freq = max(0.0, float(beh.request_frequency))

        ip_acct_combos = beh.ip_account_combinations if beh.ip_account_combinations is not None else (unique_users * unique_ips)

        return {
            "request_frequency": freq,
            "failed_request_count": float(failed),
            "failure_ratio": failure_ratio,
            "unique_users": float(unique_users),
            "unique_source_ips": float(unique_ips),
            "ip_account_combinations": float(ip_acct_combos),
            "is_login_endpoint": is_login,
            "status_code": float(req.status_code)
        }

    @staticmethod
    def to_credential_stuffing_vector(processed: Dict[str, Any]) -> np.ndarray:
        """
        Converts credential stuffing feature dict to a 1D NumPy array for model input.
        """
        return np.array([
            processed["request_frequency"],
            processed["failed_request_count"],
            processed["failure_ratio"],
            processed["unique_users"],
            processed["unique_source_ips"],
            processed["ip_account_combinations"],
            processed["is_login_endpoint"]
        ], dtype=np.float32)

    @staticmethod
    def to_bola_vector(processed: Dict[str, Any]) -> np.ndarray:
        """
        Converts BOLA feature dict to a 1D NumPy array for legacy model input.
        """
        return np.array([
            processed["unique_object_ids"],
            processed["objects_accessed_per_user"],
            processed["sequential_object_access"],
            processed["object_access_frequency"],
            processed["different_resources_accessed"],
            processed["authenticated"],
            processed["status_code"]
        ], dtype=np.float32)

    @staticmethod
    def process_bola_features(feature_set: SecurityFeatureSet) -> Dict[str, Any]:
        """
        Extracts and normalizes features relevant to BOLA / ID Enumeration.
        Features used:
        - uniqueObjectIds: Count of unique resources/object IDs accessed
        - objectsAccessedPerUser: Average objects accessed per user
        - sequentialObjectAccess: Flag indicating sequential object access pattern (0.0 or 1.0)
        - objectAccessFrequency: Rate of object accesses per minute
        - statusCodeDistribution: Flag for suspicious status codes (e.g. 403/404 scans)
        - isResourceEndpoint: 1.0 if endpoint contains parameterized resource paths (e.g. /users/{id})
        """
        req = feature_set.request_features
        beh = feature_set.behavior_features
        ident = feature_set.identity_features

        unique_objs = max(0, beh.unique_object_ids)
        if unique_objs == 0 and beh.accessed_object_ids:
            unique_objs = len(set(beh.accessed_object_ids))

        objs_per_user = beh.objects_accessed_per_user if beh.objects_accessed_per_user > 0 else float(unique_objs)
        is_sequential = 1.0 if beh.sequential_object_access else 0.0

        # Check for sequential pattern in accessed_object_ids if not explicitly set
        if not beh.sequential_object_access and beh.accessed_object_ids and len(beh.accessed_object_ids) >= 3:
            numeric_ids = []
            for obj_id in beh.accessed_object_ids:
                if str(obj_id).isdigit():
                    numeric_ids.append(int(obj_id))
            if len(numeric_ids) >= 3:
                diffs = np.diff(numeric_ids)
                if np.all(diffs == 1) or np.all(diffs == -1):
                    is_sequential = 1.0

        access_freq = max(0.0, float(beh.object_access_frequency if beh.object_access_frequency > 0 else beh.request_frequency))
        diff_resources = max(1, beh.different_resources_accessed)

        is_auth = 1.0 if ident.authenticated else 0.0

        return {
            "unique_object_ids": float(unique_objs),
            "objects_accessed_per_user": float(objs_per_user),
            "sequential_object_access": is_sequential,
            "object_access_frequency": access_freq,
            "different_resources_accessed": float(diff_resources),
            "authenticated": is_auth,
            "status_code": float(req.status_code)
        }

    @staticmethod
    def to_50_feature_dataframe(feature_set: SecurityFeatureSet) -> pd.DataFrame:
        """
        Converts SecurityFeatureSet into a 1-row Pandas DataFrame matching the 50-feature schema contract.
        """
        req = feature_set.request_features
        beh = feature_set.behavior_features
        net = feature_set.network_features
        ident = feature_set.identity_features

        endpoint_lower = req.endpoint.lower()
        is_auth = 1 if any(kw in endpoint_lower for kw in ["login", "auth", "signin", "token", "password"]) else 0
        authenticated = 1 if ident.authenticated else 0

        failed = max(0, beh.failed_request_count)
        successful = max(0, beh.successful_request_count)
        total_requests = failed + successful

        if beh.failure_ratio is not None:
            fail_ratio = float(np.clip(beh.failure_ratio, 0.0, 1.0))
        elif total_requests > 0:
            fail_ratio = float(failed / total_requests)
        elif req.status_code in [401, 403]:
            fail_ratio = 1.0
        else:
            fail_ratio = 0.0

        unique_objs = max(0, beh.unique_object_ids)
        if unique_objs == 0 and beh.accessed_object_ids:
            unique_objs = len(set(beh.accessed_object_ids))

        obj_rate = round(unique_objs / max(float(beh.request_frequency), 1.0), 3)
        seq_ratio = 1.0 if beh.sequential_object_access else 0.0

        timestamp_val = feature_set.timestamp
        if isinstance(timestamp_val, (int, float)):
            time_struct = time.gmtime(timestamp_val / 1000.0)
            timestamp_iso = time.strftime("%Y-%m-%dT%H:%M:%SZ", time_struct)
        elif isinstance(timestamp_val, str) and timestamp_val:
            timestamp_iso = timestamp_val
        else:
            time_struct = time.gmtime()
            timestamp_iso = time.strftime("%Y-%m-%dT%H:%M:%SZ", time_struct)

        hour_of_day = 12
        day_of_week = 0
        if isinstance(timestamp_val, (int, float)):
            t_struct = time.gmtime(timestamp_val / 1000.0)
            hour_of_day = t_struct.tm_hour
            day_of_week = t_struct.tm_wday

        def hash_str(val: str) -> str:
            if not val:
                return "anonymous"
            if len(val) == 12 and all(c in "0123456789abcdefABCDEF" for c in val):
                return val
            return hashlib.sha256(val.encode("utf-8")).hexdigest()[:12]

        user_id_val = getattr(ident, "user_id", None)
        session_id_val = getattr(ident, "session_id", None)
        ip_val = getattr(net, "source_ip", None) or getattr(net, "client_ip", None) or "0.0.0.0"

        row = {
            "timestamp": timestamp_iso,
            "method": req.method.upper(),
            "endpoint": req.endpoint,
            "endpoint_template": req.endpoint,
            "status_code": req.status_code,
            "response_time_ms": float(req.response_time_ms),
            "query_parameter_count": float(getattr(req, "query_params_count", 0)),
            "request_size_bytes": float(getattr(req, "request_size", 500)),
            "response_size_bytes": float(getattr(req, "response_size", 1200)),
            "authenticated": authenticated,
            "user_id_hash": hash_str(user_id_val),
            "session_id_hash": hash_str(session_id_val),
            "source_ip_hash": hash_str(ip_val),
            "user_agent": net.user_agent or "Mozilla/5.0",
            "user_profile": "CASUAL_BROWSER",
            "ip_request_count_window": float(max(1, beh.request_frequency)),
            "unique_users_per_ip": float(max(1, beh.unique_users)),
            "unique_endpoints_per_ip": float(max(1, beh.different_resources_accessed)),
            "is_authentication_request": is_auth,
            "login_attempt_count": float(total_requests if is_auth else 0),
            "login_failure_count": float(failed if is_auth else 0),
            "login_success_count": float(successful if is_auth else 0),
            "login_failure_ratio": fail_ratio,
            "unique_usernames_attempted": float(max(1, beh.unique_users)),
            "requests_last_5s": float(max(1, int(beh.request_frequency / 12))),
            "requests_last_10s": float(max(1, int(beh.request_frequency / 6))),
            "requests_last_30s": float(max(1, int(beh.request_frequency / 2))),
            "requests_last_60s": float(max(1, int(beh.request_frequency))),
            "requests_last_5m": float(max(1, int(beh.request_frequency * 5))),
            "unique_endpoints_last_60s": float(max(1, beh.different_resources_accessed)),
            "unique_objects_accessed_last_60s": float(unique_objs),
            "object_access_rate": obj_rate,
            "sequential_object_ratio": seq_ratio,
            "object_id_entropy": 0.0,
            "endpoint_entropy": 0.0,
            "request_interval_mean": 1.0,
            "request_interval_std": 0.1,
            "session_request_count": 1.0,
            "session_duration_seconds": 3.0,
            "status_4xx_ratio": 1.0 if (req.status_code >= 400 and req.status_code < 500) else 0.0,
            "status_5xx_ratio": 1.0 if req.status_code >= 500 else 0.0,
            "status_401_ratio": 1.0 if req.status_code == 401 else 0.0,
            "status_403_ratio": 1.0 if req.status_code == 403 else 0.0,
            "hour_of_day": hour_of_day,
            "day_of_week": day_of_week,
            "time_since_previous_request": 0.5,
            "burst_score": 0.2,
            "traffic_label": "NORMAL",
            "attack_type": "NONE",
            "scenario_id": "runtime-inference",
        }

        return pd.DataFrame([row])


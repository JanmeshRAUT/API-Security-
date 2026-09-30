import pytest
import pandas as pd
import numpy as np
import os
from app.ml.model_manager import model_manager
from app.features.feature_processor import FeatureProcessor
from app.models.feature_set import SecurityFeatureSet, RequestFeatures, IdentityFeatures, NetworkFeatures, BehaviorFeatures

def test_offline_live_score_match():
    # 1. Load test CSV record from evaluation pipeline
    test_csv_path = "../api-security-evaluation/data/processed_100k/test_dataset.csv"
    if not os.path.exists(test_csv_path):
        pytest.skip("Offline test CSV not found")

    df_offline = pd.read_csv(test_csv_path).head(1)
    
    # 2. Ensure model_manager is loaded
    model_manager.load_model(
        model_path="models/isolation_forest/isolation_forest_v003.joblib",
        metadata_path="models/isolation_forest/metadata_v003.json"
    )

    # 3. Direct offline transformation score
    X_proc_offline = model_manager.preprocessor.transform(df_offline)
    offline_raw_score = float(model_manager.model.decision_function(X_proc_offline)[0])

    # 4. Reconstruct SecurityFeatureSet from offline row
    row = df_offline.iloc[0]
    feature_set = SecurityFeatureSet(
        event_id="test-event-1",
        application_id="shopsphere",
        timestamp=row["timestamp"],
        request_features=RequestFeatures(
            method=row["method"],
            endpoint=row["endpoint"],
            status_code=int(row["status_code"]),
            response_time_ms=int(row["response_time_ms"]),
            request_size=int(row["request_size_bytes"]),
            response_size=int(row["response_size_bytes"]),
            query_params_count=int(row["query_parameter_count"]),
        ),
        identity_features=IdentityFeatures(
            user_id=row["user_id_hash"],
            session_id=row["session_id_hash"],
            authenticated=bool(row["authenticated"])
        ),
        network_features=NetworkFeatures(
            source_ip=row["source_ip_hash"],
            client_ip=row["source_ip_hash"],
            user_agent=row["user_agent"]
        ),
        behavior_features=BehaviorFeatures(
            request_frequency=float(row["ip_request_count_window"]),
            failed_request_count=int(row["login_failure_count"]),
            successful_request_count=int(row["login_success_count"]),
            unique_users=int(row["unique_users_per_ip"]),
            unique_source_ips=1,
            different_resources_accessed=int(row["unique_endpoints_per_ip"]),
            unique_object_ids=int(row["unique_objects_accessed_last_60s"]),
            sequential_object_access=bool(row["sequential_object_ratio"] > 0.5)
        )
    )

    # 5. Live inference pipeline transformation
    df_live = FeatureProcessor.to_50_feature_dataframe(feature_set)
    for col in df_offline.columns:
        if col in df_live.columns and col in df_offline.iloc[0]:
            df_live[col] = df_offline.iloc[0][col]

    X_proc_live = model_manager.preprocessor.transform(df_live)
    live_raw_score = float(model_manager.model.decision_function(X_proc_live)[0])

    # 6. Assert scores match within 1e-4 tolerance
    assert abs(offline_raw_score - live_raw_score) < 1e-4

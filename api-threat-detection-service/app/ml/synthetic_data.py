import numpy as np
from typing import Tuple


def generate_credential_stuffing_data(n_samples: int = 500) -> Tuple[np.ndarray, np.ndarray]:
    """
    Generates controlled synthetic training data for Credential Stuffing detection.
    Features:
    [request_frequency, failed_request_count, failure_ratio, unique_users, unique_source_ips, ip_account_combinations, is_login_endpoint]
    """
    np.random.seed(42)

    # 1. Normal Login Samples (70%)
    n_normal = int(n_samples * 0.70)
    req_freq_norm = np.random.uniform(1.0, 5.0, n_normal)
    failed_count_norm = np.random.randint(0, 3, n_normal)
    failure_ratio_norm = np.random.uniform(0.0, 0.3, n_normal)
    unique_users_norm = np.random.randint(1, 3, n_normal)
    unique_ips_norm = np.random.randint(1, 2, n_normal)
    combos_norm = unique_users_norm * unique_ips_norm
    is_login_norm = np.ones(n_normal)

    normal_data = np.column_stack([
        req_freq_norm, failed_count_norm, failure_ratio_norm,
        unique_users_norm, unique_ips_norm, combos_norm, is_login_norm
    ])

    # 2. High-Volume Credential Stuffing Samples (20%)
    n_attack = int(n_samples * 0.20)
    req_freq_att = np.random.uniform(20.0, 100.0, n_attack)
    failed_count_att = np.random.randint(15, 90, n_attack)
    failure_ratio_att = np.random.uniform(0.8, 1.0, n_attack)
    unique_users_att = np.random.randint(10, 50, n_attack)
    unique_ips_att = np.random.randint(5, 30, n_attack)
    combos_att = unique_users_att * unique_ips_att
    is_login_att = np.ones(n_attack)

    attack_data = np.column_stack([
        req_freq_att, failed_count_att, failure_ratio_att,
        unique_users_att, unique_ips_att, combos_att, is_login_att
    ])

    # 3. Low-and-Slow Distributed Credential Stuffing Samples (10%)
    # Key signal: individually low frequency (2-5 req/min), but high failure ratio, many accounts, multiple IPs
    n_slow = n_samples - n_normal - n_attack
    req_freq_slow = np.random.uniform(2.0, 6.0, n_slow)
    failed_count_slow = np.random.randint(4, 12, n_slow)
    failure_ratio_slow = np.random.uniform(0.75, 1.0, n_slow)
    unique_users_slow = np.random.randint(5, 15, n_slow)
    unique_ips_slow = np.random.randint(3, 10, n_slow)
    combos_slow = unique_users_slow * unique_ips_slow
    is_login_slow = np.ones(n_slow)

    slow_data = np.column_stack([
        req_freq_slow, failed_count_slow, failure_ratio_slow,
        unique_users_slow, unique_ips_slow, combos_slow, is_login_slow
    ])

    X = np.vstack([normal_data, attack_data, slow_data])
    # Labels: 1 for normal, -1 for anomaly
    y = np.ones(n_samples)
    y[n_normal:] = -1

    return X, y


def generate_bola_data(n_samples: int = 500) -> Tuple[np.ndarray, np.ndarray]:
    """
    Generates controlled synthetic training data for BOLA / ID Enumeration detection.
    Features:
    [unique_object_ids, objects_accessed_per_user, sequential_object_access, object_access_frequency, different_resources_accessed, authenticated]
    """
    np.random.seed(42)

    # 1. Normal Object Access (75%)
    n_normal = int(n_samples * 0.75)
    unique_objs_norm = np.random.randint(1, 4, n_normal)
    objs_per_user_norm = np.random.uniform(1.0, 3.0, n_normal)
    seq_norm = np.zeros(n_normal)  # random or repeated, not sequential
    freq_norm = np.random.uniform(1.0, 5.0, n_normal)
    resources_norm = np.random.randint(1, 2, n_normal)
    auth_norm = np.ones(n_normal)

    normal_data = np.column_stack([
        unique_objs_norm, objs_per_user_norm, seq_norm,
        freq_norm, resources_norm, auth_norm
    ])

    # 2. Suspicious Sequential BOLA Enumeration (25%)
    n_bola = n_samples - n_normal
    unique_objs_bola = np.random.randint(10, 50, n_bola)
    objs_per_user_bola = np.random.uniform(10.0, 45.0, n_bola)
    seq_bola = np.random.choice([1.0, 1.0, 0.0], size=n_bola)  # mostly sequential
    freq_bola = np.random.uniform(15.0, 60.0, n_bola)
    resources_bola = np.random.randint(1, 3, n_bola)
    auth_bola = np.random.choice([1.0, 0.0], size=n_bola)

    bola_data = np.column_stack([
        unique_objs_bola, objs_per_user_bola, seq_bola,
        freq_bola, resources_bola, auth_bola
    ])

    X = np.vstack([normal_data, bola_data])
    y = np.ones(n_samples)
    y[n_normal:] = -1

    return X, y

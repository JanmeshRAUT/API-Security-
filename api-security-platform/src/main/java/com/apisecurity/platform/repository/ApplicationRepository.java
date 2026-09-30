package com.apisecurity.platform.repository;

import com.apisecurity.platform.domain.application.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, String> {
    Optional<ApplicationEntity> findByApplicationId(String applicationId);
    Optional<ApplicationEntity> findByApiKeyHash(String apiKeyHash);
    boolean existsByApplicationId(String applicationId);
}

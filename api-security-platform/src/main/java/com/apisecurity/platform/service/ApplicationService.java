package com.apisecurity.platform.service;

import com.apisecurity.platform.domain.application.ApplicationEntity;
import com.apisecurity.platform.domain.application.ApplicationStatus;
import com.apisecurity.platform.dto.ApplicationRegistrationRequest;
import com.apisecurity.platform.dto.ApplicationRegistrationResponse;
import com.apisecurity.platform.dto.ApplicationResponse;
import com.apisecurity.platform.exception.ResourceNotFoundException;
import com.apisecurity.platform.mapper.ApplicationMapper;
import com.apisecurity.platform.repository.ApplicationRepository;
import com.apisecurity.platform.security.ApiKeyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;

    public ApplicationService(ApplicationRepository applicationRepository, ApplicationMapper applicationMapper) {
        this.applicationRepository = applicationRepository;
        this.applicationMapper = applicationMapper;
    }

    @Transactional
    public ApplicationRegistrationResponse registerApplication(ApplicationRegistrationRequest request) {
        if (applicationRepository.existsByApplicationId(request.getApplicationId())) {
            throw new IllegalArgumentException("Application ID already exists: " + request.getApplicationId());
        }

        String rawApiKey = ApiKeyUtils.generateApiKey();
        String hashedApiKey = ApiKeyUtils.hashApiKey(rawApiKey);

        ApplicationEntity entity = ApplicationEntity.builder()
                .name(request.getName())
                .applicationId(request.getApplicationId())
                .apiKeyHash(hashedApiKey)
                .description(request.getDescription())
                .status(ApplicationStatus.ACTIVE)
                .build();

        ApplicationEntity saved = applicationRepository.save(entity);
        return applicationMapper.toRegistrationResponse(saved, rawApiKey);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAll().stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(String idOrApplicationId) {
        ApplicationEntity entity = applicationRepository.findById(idOrApplicationId)
                .or(() -> applicationRepository.findByApplicationId(idOrApplicationId))
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + idOrApplicationId));
        return applicationMapper.toResponse(entity);
    }

    @Transactional
    public void deleteApplication(String idOrApplicationId) {
        ApplicationEntity entity = applicationRepository.findById(idOrApplicationId)
                .or(() -> applicationRepository.findByApplicationId(idOrApplicationId))
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + idOrApplicationId));
        applicationRepository.delete(entity);
    }

    @Transactional
    public ApplicationRegistrationResponse rotateApiKey(String idOrApplicationId) {
        ApplicationEntity entity = applicationRepository.findById(idOrApplicationId)
                .or(() -> applicationRepository.findByApplicationId(idOrApplicationId))
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + idOrApplicationId));

        String rawApiKey = ApiKeyUtils.generateApiKey();
        String hashedApiKey = ApiKeyUtils.hashApiKey(rawApiKey);

        entity.setApiKeyHash(hashedApiKey);
        ApplicationEntity saved = applicationRepository.save(entity);

        return applicationMapper.toRegistrationResponse(saved, rawApiKey);
    }

    @Transactional(readOnly = true)
    public Optional<ApplicationEntity> validateApiKey(String rawApiKey) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            return Optional.empty();
        }
        String hash = ApiKeyUtils.hashApiKey(rawApiKey);
        return applicationRepository.findByApiKeyHash(hash)
                .filter(app -> app.getStatus() == ApplicationStatus.ACTIVE);
    }

    @Transactional
    public ApplicationEntity getOrCreateApplication(String applicationId, String rawApiKey) {
        if (applicationId == null || applicationId.isBlank()) {
            applicationId = "default-service";
        }
        final String targetAppId = applicationId.trim();
        return applicationRepository.findByApplicationId(targetAppId)
                .orElseGet(() -> {
                    String key = (rawApiKey != null && !rawApiKey.isBlank()) ? rawApiKey : ApiKeyUtils.generateApiKey();
                    String hashed = ApiKeyUtils.hashApiKey(key);
                    String readableName = targetAppId.substring(0, 1).toUpperCase() + 
                            (targetAppId.length() > 1 ? targetAppId.substring(1).replace("-", " ").replace("_", " ") : "");
                    ApplicationEntity newApp = ApplicationEntity.builder()
                            .name(readableName)
                            .applicationId(targetAppId)
                            .apiKeyHash(hashed)
                            .description("Auto-discovered service: " + targetAppId)
                            .status(ApplicationStatus.ACTIVE)
                            .lastSeenAt(LocalDateTime.now())
                            .build();
                    return applicationRepository.save(newApp);
                });
    }

    @Transactional
    public void updateLastSeen(String applicationId) {
        applicationRepository.findByApplicationId(applicationId)
                .ifPresent(app -> {
                    app.setLastSeenAt(LocalDateTime.now());
                    applicationRepository.save(app);
                });
    }
}

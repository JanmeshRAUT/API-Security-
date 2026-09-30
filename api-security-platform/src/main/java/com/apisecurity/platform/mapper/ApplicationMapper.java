package com.apisecurity.platform.mapper;

import com.apisecurity.platform.domain.application.ApplicationEntity;
import com.apisecurity.platform.dto.ApplicationRegistrationResponse;
import com.apisecurity.platform.dto.ApplicationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApplicationMapper {

    ApplicationResponse toResponse(ApplicationEntity entity);

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "name", source = "entity.name")
    @Mapping(target = "applicationId", source = "entity.applicationId")
    @Mapping(target = "apiKey", source = "rawApiKey")
    @Mapping(target = "description", source = "entity.description")
    @Mapping(target = "status", source = "entity.status")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    ApplicationRegistrationResponse toRegistrationResponse(ApplicationEntity entity, String rawApiKey);
}

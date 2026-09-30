package com.apisecurity.platform.mapper;

import com.apisecurity.platform.domain.application.ApplicationEntity;
import com.apisecurity.platform.dto.ApplicationRegistrationResponse;
import com.apisecurity.platform.dto.ApplicationResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-29T19:08:12+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class ApplicationMapperImpl implements ApplicationMapper {

    @Override
    public ApplicationResponse toResponse(ApplicationEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ApplicationResponse.ApplicationResponseBuilder applicationResponse = ApplicationResponse.builder();

        applicationResponse.id( entity.getId() );
        applicationResponse.name( entity.getName() );
        applicationResponse.applicationId( entity.getApplicationId() );
        applicationResponse.description( entity.getDescription() );
        if ( entity.getStatus() != null ) {
            applicationResponse.status( entity.getStatus().name() );
        }
        applicationResponse.createdAt( entity.getCreatedAt() );
        applicationResponse.updatedAt( entity.getUpdatedAt() );
        applicationResponse.lastSeenAt( entity.getLastSeenAt() );

        return applicationResponse.build();
    }

    @Override
    public ApplicationRegistrationResponse toRegistrationResponse(ApplicationEntity entity, String rawApiKey) {
        if ( entity == null && rawApiKey == null ) {
            return null;
        }

        ApplicationRegistrationResponse.ApplicationRegistrationResponseBuilder applicationRegistrationResponse = ApplicationRegistrationResponse.builder();

        if ( entity != null ) {
            applicationRegistrationResponse.id( entity.getId() );
            applicationRegistrationResponse.name( entity.getName() );
            applicationRegistrationResponse.applicationId( entity.getApplicationId() );
            applicationRegistrationResponse.description( entity.getDescription() );
            if ( entity.getStatus() != null ) {
                applicationRegistrationResponse.status( entity.getStatus().name() );
            }
            applicationRegistrationResponse.createdAt( entity.getCreatedAt() );
        }
        applicationRegistrationResponse.apiKey( rawApiKey );

        return applicationRegistrationResponse.build();
    }
}

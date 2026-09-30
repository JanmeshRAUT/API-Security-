package com.apisecurity.platform.mapper;

import com.apisecurity.platform.domain.event.SecurityEventEntity;
import com.apisecurity.platform.dto.EventResponseDto;
import com.apisecurity.platform.dto.realtime.RealtimeSecurityEventDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-29T19:08:12+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class SecurityEventMapperImpl implements SecurityEventMapper {

    @Override
    public EventResponseDto toResponseDto(SecurityEventEntity entity) {
        if ( entity == null ) {
            return null;
        }

        EventResponseDto.EventResponseDtoBuilder eventResponseDto = EventResponseDto.builder();

        eventResponseDto.id( entity.getId() );
        eventResponseDto.eventId( entity.getEventId() );
        eventResponseDto.applicationId( entity.getApplicationId() );
        eventResponseDto.timestamp( entity.getTimestamp() );
        eventResponseDto.requestId( entity.getRequestId() );
        eventResponseDto.httpMethod( entity.getHttpMethod() );
        eventResponseDto.endpoint( entity.getEndpoint() );
        eventResponseDto.clientIp( entity.getClientIp() );
        eventResponseDto.sourceIpHash( entity.getSourceIpHash() );
        eventResponseDto.userHash( entity.getUserHash() );
        eventResponseDto.authenticated( entity.isAuthenticated() );
        eventResponseDto.statusCode( entity.getStatusCode() );
        eventResponseDto.responseTimeMs( entity.getResponseTimeMs() );
        eventResponseDto.requestSize( entity.getRequestSize() );
        eventResponseDto.responseSize( entity.getResponseSize() );
        eventResponseDto.userAgent( entity.getUserAgent() );
        eventResponseDto.processingStatus( entity.getProcessingStatus() );
        eventResponseDto.createdAt( entity.getCreatedAt() );

        return eventResponseDto.build();
    }

    @Override
    public RealtimeSecurityEventDto toRealtimeDto(SecurityEventEntity entity) {
        if ( entity == null ) {
            return null;
        }

        RealtimeSecurityEventDto.RealtimeSecurityEventDtoBuilder realtimeSecurityEventDto = RealtimeSecurityEventDto.builder();

        realtimeSecurityEventDto.id( entity.getId() );
        realtimeSecurityEventDto.eventId( entity.getEventId() );
        realtimeSecurityEventDto.applicationId( entity.getApplicationId() );
        realtimeSecurityEventDto.timestamp( entity.getTimestamp() );
        realtimeSecurityEventDto.httpMethod( entity.getHttpMethod() );
        realtimeSecurityEventDto.endpoint( entity.getEndpoint() );
        realtimeSecurityEventDto.clientIp( entity.getClientIp() );
        realtimeSecurityEventDto.statusCode( entity.getStatusCode() );
        realtimeSecurityEventDto.responseTimeMs( entity.getResponseTimeMs() );
        realtimeSecurityEventDto.requestSize( entity.getRequestSize() );
        realtimeSecurityEventDto.responseSize( entity.getResponseSize() );
        realtimeSecurityEventDto.authenticated( entity.isAuthenticated() );
        realtimeSecurityEventDto.sourceIpHash( entity.getSourceIpHash() );
        realtimeSecurityEventDto.userHash( entity.getUserHash() );

        return realtimeSecurityEventDto.build();
    }
}

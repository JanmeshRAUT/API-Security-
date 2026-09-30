package com.apisecurity.platform.mapper;

import com.apisecurity.platform.domain.threat.ThreatEntity;
import com.apisecurity.platform.dto.ThreatResponseDto;
import com.apisecurity.platform.dto.realtime.RealtimeThreatDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-29T19:08:12+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class ThreatMapperImpl extends ThreatMapper {

    @Override
    public ThreatResponseDto toResponseDto(ThreatEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ThreatResponseDto.ThreatResponseDtoBuilder threatResponseDto = ThreatResponseDto.builder();

        threatResponseDto.reasonCodes( jsonToList( entity.getReasonCodes() ) );
        threatResponseDto.id( entity.getId() );
        threatResponseDto.eventId( entity.getEventId() );
        threatResponseDto.applicationId( entity.getApplicationId() );
        threatResponseDto.threatType( entity.getThreatType() );
        threatResponseDto.confidence( entity.getConfidence() );
        threatResponseDto.riskScore( entity.getRiskScore() );
        threatResponseDto.severity( entity.getSeverity() );
        threatResponseDto.recommendedAction( entity.getRecommendedAction() );
        threatResponseDto.status( entity.getStatus() );
        threatResponseDto.detectedAt( entity.getDetectedAt() );
        threatResponseDto.endpoint( entity.getEndpoint() );
        threatResponseDto.sourceIpHash( entity.getSourceIpHash() );
        threatResponseDto.userHash( entity.getUserHash() );
        threatResponseDto.httpMethod( entity.getHttpMethod() );
        threatResponseDto.statusCode( entity.getStatusCode() );

        return threatResponseDto.build();
    }

    @Override
    public RealtimeThreatDto toRealtimeDto(ThreatEntity entity) {
        if ( entity == null ) {
            return null;
        }

        RealtimeThreatDto.RealtimeThreatDtoBuilder realtimeThreatDto = RealtimeThreatDto.builder();

        realtimeThreatDto.id( entity.getId() );
        realtimeThreatDto.threatId( entity.getId() );
        realtimeThreatDto.reasonCodes( jsonToList( entity.getReasonCodes() ) );
        realtimeThreatDto.eventId( entity.getEventId() );
        realtimeThreatDto.applicationId( entity.getApplicationId() );
        realtimeThreatDto.threatType( entity.getThreatType() );
        realtimeThreatDto.riskScore( entity.getRiskScore() );
        realtimeThreatDto.confidence( entity.getConfidence() );
        realtimeThreatDto.severity( entity.getSeverity() );
        realtimeThreatDto.endpoint( entity.getEndpoint() );
        realtimeThreatDto.detectedAt( entity.getDetectedAt() );
        realtimeThreatDto.recommendedAction( entity.getRecommendedAction() );
        realtimeThreatDto.status( entity.getStatus() );

        return realtimeThreatDto.build();
    }
}

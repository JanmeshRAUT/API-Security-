package com.apisecurity.platform.mapper;

import com.apisecurity.platform.domain.event.SecurityEventEntity;
import com.apisecurity.platform.dto.EventResponseDto;
import com.apisecurity.platform.dto.realtime.RealtimeSecurityEventDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SecurityEventMapper {

    EventResponseDto toResponseDto(SecurityEventEntity entity);

    RealtimeSecurityEventDto toRealtimeDto(SecurityEventEntity entity);
}

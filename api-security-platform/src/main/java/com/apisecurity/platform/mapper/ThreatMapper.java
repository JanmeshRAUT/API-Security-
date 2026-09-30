package com.apisecurity.platform.mapper;

import com.apisecurity.platform.domain.threat.ThreatEntity;
import com.apisecurity.platform.dto.ThreatResponseDto;
import com.apisecurity.platform.dto.realtime.RealtimeThreatDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ThreatMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "reasonCodes", source = "reasonCodes", qualifiedByName = "jsonToList")
    public abstract ThreatResponseDto toResponseDto(ThreatEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "threatId", source = "id")
    @Mapping(target = "reasonCodes", source = "reasonCodes", qualifiedByName = "jsonToList")
    public abstract RealtimeThreatDto toRealtimeDto(ThreatEntity entity);

    @Named("jsonToList")
    protected List<String> jsonToList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of(json);
        }
    }
}

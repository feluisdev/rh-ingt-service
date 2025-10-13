package cv.igrp.RH_Service.options.infrastructure.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.igrp.RH_Service.options.domain.valueobject.Metadata;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MetadataMapper {

    private final ObjectMapper objectMapper;

    public MetadataMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Metadata toDomain(String json) {
        if (json == null || json.isEmpty()) {
            return Metadata.vazio();
        }

        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            return Metadata.fromMap(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao desserializar metadata JSON", e);
        }
    }

    public String toEntity(Metadata metadata) {
        if (metadata == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(metadata.getValores());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar Metadata para JSON", e);
        }
    }
}

package cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers;

import cv.igrp.RH_Service.parametrizacoes.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.OptionEntity;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import org.springframework.stereotype.Component;

@Component
public class OptionMapper {

    public OptionEntity toEntity(Option domain) {
        if (domain == null) return null;
        OptionEntity entity = new OptionEntity();
        entity.setId(domain.getId().getValor());
        entity.setCcode(domain.getCcode());
        entity.setCkey(domain.getCkey());
        entity.setCvalue(domain.getCvalue());
        entity.setLocale(domain.getLocale());
        entity.setSortOrder(domain.getSortOrder());
        entity.setActive(domain.isActive());
        entity.setDescription(domain.getDescription());
        return entity;
    }

    public Option toDomain(OptionEntity entity) {
        if (entity == null) return null;
        return Option.reconstruir(
            ExternalID.from(entity.getId()),
            entity.getCcode(),
            entity.getCkey(),
            entity.getCvalue(),
            entity.getLocale(),
            entity.getSortOrder(),
            entity.isActive(),
            entity.getDescription()
        );
    }

    public OptionResponseDTO toDTO(Option domain) {
        if (domain == null) return null;
        var dto = new OptionResponseDTO();
        dto.setOptionId(domain.getId().getStringValor());
        dto.setCcode(domain.getCcode());
        dto.setCkey(domain.getCkey());
        dto.setCvalue(domain.getCvalue());
        dto.setLocale(domain.getLocale());
        dto.setSortOrder(domain.getSortOrder());
        dto.setActive(domain.isActive());
        dto.setDescription(domain.getDescription());
        return dto;
    }
}

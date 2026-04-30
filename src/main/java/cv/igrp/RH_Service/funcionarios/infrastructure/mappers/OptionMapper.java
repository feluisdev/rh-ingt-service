package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Option;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.OptionEntity;
import org.springframework.stereotype.Component;

@Component("funcionariosOptionMapper")
public class OptionMapper {

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

  public OptionEntity toEntity(Option domain) {
    if (domain == null) return null;

    OptionEntity entity = new OptionEntity();
    entity.setId(domain.getIdOption().getValor());
    entity.setCcode(domain.getCcode());
    entity.setCkey(domain.getCkey());
    entity.setCvalue(domain.getCvalue());
    entity.setLocale(domain.getLocale());
    entity.setSortOrder(domain.getSortOrder());
    entity.setActive(domain.isActive());
    entity.setDescription(domain.getDescription());
    return entity;
  }

  public OptionResponseDTO toDTO(Option option) {
    if (option == null) return null;

    OptionResponseDTO dto = new OptionResponseDTO();
    dto.setId(option.getIdOption() != null ? option.getIdOption().getStringValor() : null);
    dto.setCcode(option.getCcode());
    dto.setCkey(option.getCkey());
    dto.setCvalue(option.getCvalue());
    dto.setLocale(option.getLocale());
    dto.setSort_order(option.getSortOrder());
    dto.setActive(option.isActive());
    dto.setDescription(option.getDescription());
    return dto;
  }
}

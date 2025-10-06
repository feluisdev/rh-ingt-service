package cv.igrp.RH_Service.options.infrastructure.mappers;


import cv.igrp.RH_Service.options.application.dto.OptionCodeItemDTO;
import cv.igrp.RH_Service.options.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.options.domain.models.Option;
import cv.igrp.RH_Service.options.domain.valueobject.OptionId;
import cv.igrp.RH_Service.options.infrastructure.persistence.entity.OptionsEntity;
import org.springframework.stereotype.Component;

@Component
public class OptionMapper {
  private final MetadataMapper metadataMapper;

  public OptionMapper(MetadataMapper metadataMapper){
    this.metadataMapper = metadataMapper;
  }

  public Option toDomain(OptionsEntity entity) {
    if (entity == null) return null;

    return Option.rebuild(
        OptionId.from(entity.getId()),
        entity.getCcode(),
        entity.getCkey(),
        entity.getCvalue(),
        entity.getLocale(),
        entity.getSortOrder(),
        entity.isActive(),
        metadataMapper.toDomain(entity.getMetadata()),
        entity.getDescription()
    );
  }

  public OptionsEntity toEntity(Option domain) {
    if (domain == null) return null;

    OptionsEntity entity = new OptionsEntity();
    entity.setId(domain.getId().getIdentificador().getValor());
    entity.setCcode(domain.getCcode());
    entity.setCkey(domain.getCkey());
    entity.setCvalue(domain.getCvalue());
    entity.setLocale(domain.getLocale());
    entity.setSortOrder(domain.getSort_order());
    entity.setActive(domain.isEnabled());
    entity.setMetadata(metadataMapper.toEntity(domain.getMetadata()));
    entity.setDescription(domain.getDescription());

    return entity;
  }

  public OptionResponseDTO toResponseDTO(Option option) {
    if (option == null) return null;

    OptionResponseDTO dto = new OptionResponseDTO();
    dto.setId(option.getId().getIdentificador().getStringValor());
    dto.setCcode(option.getCcode());
    dto.setCkey(option.getCkey());
    dto.setCvalue(option.getCvalue());
    dto.setLocale(option.getLocale());
    dto.setSort_order(option.getSort_order());
    dto.setDescription(option.getDescription());
    return dto;
  }

  public OptionCodeItemDTO toOptionCodeItemDTO(Option option) {
    if (option == null) return null;

    OptionCodeItemDTO dto = new OptionCodeItemDTO();
    dto.setKey(option.getCkey());
    dto.setSortOrder(option.getSort_order());
    dto.setMetadata(option.getMetadata().getValores());
    dto.setValue(option.getCvalue());
    dto.setDescription(option.getDescription());

    return dto;
  }
}

package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.domain.models.Cargo;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.CargoEntity;
import org.springframework.stereotype.Component;

@Component
public class CargoMapper {

  public Cargo toDomain(CargoEntity entity) {
    if (entity == null) return null;

    return Cargo.reconstruir(
        entity.getId(),
        ExternalID.from(entity.getExternalId()),
        entity.getNome(),
        entity.getDescricao(),
        entity.getSalarioBase(),
        entity.getNivelHierarquico(),
        entity.getEstado()
    );
  }

  public CargoEntity toEntity(Cargo domain) {
    if (domain == null) return null;

    CargoEntity entity = new CargoEntity();

    entity.setId(domain.getId());
    entity.setExternalId(domain.getExternalId().getValor());
    entity.setNome(domain.getNome());
    entity.setDescricao(domain.getDescricao());
    entity.setSalarioBase(domain.getSalarioBase());
    entity.setNivelHierarquico(domain.getNivelHierarquico());
    entity.setEstado(domain.getEstado());

    return entity;
  }
}

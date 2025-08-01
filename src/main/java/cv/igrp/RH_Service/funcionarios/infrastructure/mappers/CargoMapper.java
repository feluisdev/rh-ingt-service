package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.application.dto.CargoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Cargo;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.CargoEntity;
import org.springframework.stereotype.Component;

@Component
public class CargoMapper {

  public Cargo toDomain(CargoEntity entity) {
    if (entity == null) return null;

    return Cargo.reconstruir(
        ExternalID.from(entity.getId()),
        entity.getNome(),
        entity.getCodigo(),
        entity.getDescricao(),
        entity.getSalarioBase(),
        entity.getNivelHierarquico(),
        entity.getEstado()
    );
  }

  public CargoEntity toEntity(Cargo domain) {
    if (domain == null) return null;

    CargoEntity entity = new CargoEntity();

    entity.setId(domain.getIdCargo().getValor());
    entity.setNome(domain.getNome());
    entity.setDescricao(domain.getDescricao());
    entity.setSalarioBase(domain.getSalarioBase());
    entity.setNivelHierarquico(domain.getNivelHierarquico());
    entity.setEstado(domain.getEstado());
    entity.setCodigo(domain.getCodigo());

    return entity;
  }

  public CargoResponseDTO toDTO(Cargo cargo) {
    if (cargo == null) {
      return null;
    }

    CargoResponseDTO dto = new CargoResponseDTO();

    dto.setCargoId(cargo.getIdCargo() != null ? cargo.getIdCargo().getStringValor() : null);
    dto.setNome(cargo.getNome());
    dto.setDescricao(cargo.getDescricao());
    dto.setCodigo(cargo.getCodigo());
    dto.setSalarioBase(cargo.getSalarioBase());
    dto.setNivelHierarquico(cargo.getNivelHierarquico());
    dto.setEstado(cargo.getEstado() != null ? cargo.getEstado().name() : null);
    dto.setEstadoDesc(cargo.getEstado() != null ? cargo.getEstado().getDescription() : null);

    return dto;
  }

}

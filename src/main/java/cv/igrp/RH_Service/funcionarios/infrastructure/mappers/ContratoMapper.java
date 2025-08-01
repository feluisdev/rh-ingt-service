package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.application.dto.ContratoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Cargo;
import cv.igrp.RH_Service.funcionarios.domain.models.Contrato;
import cv.igrp.RH_Service.funcionarios.domain.models.Departamento;
import cv.igrp.RH_Service.funcionarios.domain.models.Funcionario;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.CargoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.ContratoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DepartamentoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FuncionarioEntity;
import org.springframework.stereotype.Component;

@Component
public class ContratoMapper {



  public Contrato toDomain(ContratoEntity entity) {
    if (entity == null) return null;

    return Contrato.reconstruir(
        ExternalID.from( entity.getId()),
        entity.getTipoContrato(),
        entity.getDataInicio(),
        entity.getDataFim(),
        entity.getSalario(),
        entity.getCargaHoraria(),
        entity.getObservacoes(),
        entity.getEstado(),
        ExternalID.from(entity.getIdDepartamento().getId()),
            ExternalID.from(entity.getIdFuncionario().getId()),
                ExternalID.from(entity.getIdCargo().getId())
    );
  }

  // ====== DOMAIN → ENTITY ======
  public ContratoEntity toEntity(Contrato domain) {
    if (domain == null) return null;

    ContratoEntity entity = new ContratoEntity();
    entity.setId(domain.getIdContrato().getValor());
    entity.setTipoContrato(domain.getTipoContrato());
    entity.setDataInicio(domain.getDataInicio());
    entity.setDataFim(domain.getDataFim());
    entity.setSalario(domain.getSalario());
    entity.setCargaHoraria(domain.getCargaHoraria());
    entity.setObservacoes(domain.getObservacoes());
    entity.setEstado(domain.getEstado());

    DepartamentoEntity departamentoEntity = new DepartamentoEntity();
    departamentoEntity.setId(domain.getDepartamentoId().getValor());
    entity.setIdDepartamento(departamentoEntity);

    FuncionarioEntity funcionarioEntity = new FuncionarioEntity();
    funcionarioEntity.setId(domain.getFuncionarioId().getValor());
    entity.setIdFuncionario(funcionarioEntity);

    CargoEntity cargoEntity = new CargoEntity();
    cargoEntity.setId(domain.getCargoId().getValor());
    entity.setIdCargo(cargoEntity);

    return entity;
  }


  public ContratoResponseDTO toDTO(Contrato contrato) {
    if (contrato == null) return null;

    return new ContratoResponseDTO(
        contrato.getIdContrato() != null ? contrato.getIdContrato().getStringValor() : null,
        contrato.getFuncionarioId() != null ? contrato.getFuncionarioId().getStringValor() : null,
        contrato.getDepartamentoId() != null ? contrato.getDepartamentoId().getStringValor() : null,
        contrato.getCargoId() != null ? contrato.getCargoId().getStringValor() : null,
        contrato.getTipoContrato().getCode(),
        contrato.getTipoContrato().getDescription(),
        contrato.getDataInicio(),
        contrato.getDataFim(),
        contrato.getSalario(),
        contrato.getCargaHoraria(),
        contrato.getObservacoes(),
        contrato.getEstado().getCode(),
        contrato.getEstado().getDescription(), // Assumindo que existe um getEstadoDescricao()
        null
    );
  }
}

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

  // ====== ENTITY → DOMAIN ======
  public Contrato toDomainComReferencias(ContratoEntity entity,
                                         Departamento departamento,
                                         Funcionario funcionario,
                                         Cargo cargo) {
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
        departamento,
        funcionario,
        cargo
    );
  }

  // ====== DOMAIN → ENTITY ======
  public ContratoEntity toEntity(Contrato domain, DepartamentoEntity departamentoEntity, FuncionarioEntity funcionarioEntity, CargoEntity cargoEntity) {
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

    entity.setIdDepartamento(departamentoEntity);
    entity.setIdFuncionario(funcionarioEntity);
    entity.setIdCargo(cargoEntity);

    return entity;
  }

  public ContratoResponseDTO toDTO(Contrato contrato) {
    if (contrato == null) return null;

    return new ContratoResponseDTO(
        contrato.getIdContrato() != null ? contrato.getIdContrato().getStringValor() : null,
        contrato.getFuncionario() != null ? contrato.getFuncionario().getExternalId().getStringValor() : null,
        contrato.getDepartamento() != null ? contrato.getDepartamento().getIdDepartamento().getStringValor() : null,
        contrato.getCargo() != null ? contrato.getCargo().getIdCargo().getStringValor() : null,
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

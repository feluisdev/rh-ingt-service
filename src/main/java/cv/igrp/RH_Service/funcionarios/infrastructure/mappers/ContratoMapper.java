package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.application.dto.ContratoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.*;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.*;
import org.springframework.stereotype.Component;

@Component
public class ContratoMapper {

  private final DocumentoMapper documentoMapper;

  private final DepartamentoMapper departamentoMapper;
  private final CargoMapper cargoMapper;

  public ContratoMapper(DocumentoMapper documentoMapper, DepartamentoMapper departamentoMapper, CargoMapper cargoMapper) {
    this.documentoMapper = documentoMapper;
    this.departamentoMapper = departamentoMapper;
    this.cargoMapper = cargoMapper;
  }



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
        ExternalID.from(entity.getIdFuncionario().getId()),
        cargoMapper.toDomain(entity.getIdCargo()),
        departamentoMapper.toDomain(entity.getIdDepartamento()),
        null
    );
  }

  public Contrato toDomain(ContratoEntity entity, DocumentoEntity documentoEntity) {
    if (entity == null) return null;

    Documento contratoAnexo = documentoEntity != null
        ? documentoMapper.toDomain(documentoEntity)
        : null;

    return Contrato.reconstruir(
        ExternalID.from( entity.getId()),
        entity.getTipoContrato(),
        entity.getDataInicio(),
        entity.getDataFim(),
        entity.getSalario(),
        entity.getCargaHoraria(),
        entity.getObservacoes(),
        entity.getEstado(),
        ExternalID.from(entity.getIdFuncionario().getId()),
        cargoMapper.toDomain(entity.getIdCargo()),
        departamentoMapper.toDomain(entity.getIdDepartamento()),
        contratoAnexo
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

    entity.setIdDepartamento(departamentoMapper.toEntity(domain.getDepartamento()));
    entity.setIdCargo(cargoMapper.toEntity(domain.getCargo()));

    FuncionarioEntity funcionarioEntity = new FuncionarioEntity();
    funcionarioEntity.setId(domain.getFuncionarioId().getValor());
    entity.setIdFuncionario(funcionarioEntity);

    return entity;
  }


  public ContratoResponseDTO toDTO(Contrato contrato) {
    if (contrato == null) return null;

    var dto = new ContratoResponseDTO();

    dto.setContratoId(contrato.getIdContrato().getStringValor());
    dto.setFuncionarioId(contrato.getFuncionarioId().getStringValor());
    dto.setDepartamentoId(contrato.getDepartamento().getIdDepartamento().getStringValor());
    dto.setCargoId(contrato.getCargo().getIdCargo().getStringValor());
    dto.setTipoContrato(contrato.getTipoContrato().getCode());
    dto.setTipoContratoDesc(contrato.getTipoContrato().getDescription());
    dto.setDataInicio(contrato.getDataInicio());
    dto.setDataFim(contrato.getDataFim());
    dto.setSalario(contrato.getSalario());
    dto.setCargaHoraria(contrato.getCargaHoraria());
    dto.setObservacoes(contrato.getObservacoes());
    dto.setEstado(contrato.getEstado().getCode());
    dto.setEstadoDesc(contrato.getEstado().getDescription());

    if (contrato.getContratoAnexo() != null) {
      dto.setAnexo(documentoMapper.toDTO(contrato.getContratoAnexo()));
    }

    return dto;
  }

}

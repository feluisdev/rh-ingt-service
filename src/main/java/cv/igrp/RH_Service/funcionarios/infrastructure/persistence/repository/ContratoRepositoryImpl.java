package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository;

import cv.igrp.RH_Service.funcionarios.domain.models.Contrato;
import cv.igrp.RH_Service.funcionarios.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.CargoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.ContratoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DepartamentoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.ContratoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ContratoRepositoryImpl implements ContratoRepository {
  private final ContratoEntityRepository contratoEntityRepository;

  private final ContratoMapper contratoMapper;
  private final DepartamentoMapper departamentoMapper;
  private final FuncionarioMapper funcionarioMapper;
  private final CargoMapper cargoMapper;

  @Transactional
  @Override
  public Contrato save(Contrato contrato) {
    var cargoEntity = cargoMapper.toEntity(contrato.getCargo());
    var funcionarioEntity = funcionarioMapper.toEntity(contrato.getFuncionario());
    var departamentoEntity = departamentoMapper.toEntity(contrato.getDepartamento(), funcionarioEntity);

    var entity = contratoMapper.toEntity(contrato, departamentoEntity, funcionarioEntity, cargoEntity);
    var saved = contratoEntityRepository.save(entity);

    return contratoMapper.toDomainComReferencias(saved, contrato.getDepartamento(), contrato.getFuncionario(), contrato.getCargo());
  }


  @Transactional(readOnly = true)
  @Override
  public Optional<Contrato> getById(ExternalID contratoId) {
    return contratoEntityRepository.findByExternalId(contratoId.getValor())
        .map(entity -> {
          var funcionario = funcionarioMapper.toLightDomain(entity.getIdFuncionario());
          var departamento = departamentoMapper.toDomainWithResponsavel(
              entity.getIdDepartamento(),
              funcionarioMapper.toLightDomain(entity.getIdDepartamento().getResponsavelId())
          );
          var cargo = cargoMapper.toDomain(entity.getIdCargo());

          return contratoMapper.toDomainComReferencias(entity, departamento, funcionario, cargo);
        });
  }

  @Transactional(readOnly = true)
  @Override
  public List<Contrato> getAll() {
    return contratoEntityRepository.findAllByEstado(Estado.A)
        .stream()
        .map(entity -> {
          var funcionario = funcionarioMapper.toLightDomain(entity.getIdFuncionario());
          var departamento = departamentoMapper.toDomainWithResponsavel(
              entity.getIdDepartamento(),
              funcionarioMapper.toLightDomain(entity.getIdDepartamento().getResponsavelId())
          );
          var cargo = cargoMapper.toDomain(entity.getIdCargo());

          return contratoMapper.toDomainComReferencias(entity, departamento, funcionario, cargo);
        })
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Contrato> getAllByFuncionariolId(ExternalID funcionarioId) {
    return contratoEntityRepository.findByIdFuncionario_Id(funcionarioId.getValor())
        .stream()
        .map(entity -> {
          var funcionario = funcionarioMapper.toLightDomain(entity.getIdFuncionario());
          var departamento = departamentoMapper.toDomainWithResponsavel(
              entity.getIdDepartamento(),
              funcionarioMapper.toLightDomain(entity.getIdDepartamento().getResponsavelId())
          );
          var cargo = cargoMapper.toDomain(entity.getIdCargo());

          return contratoMapper.toDomainComReferencias(entity, departamento, funcionario, cargo);
        })
        .toList();
  }
}

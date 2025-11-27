package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository;

import cv.igrp.RH_Service.funcionarios.domain.models.Contrato;
import cv.igrp.RH_Service.funcionarios.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.*;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.application.constants.ObjetoTipo;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DocumentoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.ContratoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.DocumentoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ContratoRepositoryImpl implements ContratoRepository {
  private final ContratoEntityRepository contratoEntityRepository;

  private final ContratoMapper contratoMapper;
  private final DepartamentoMapper departamentoMapper;
  private final FuncionarioMapper funcionarioMapper;
  private final CargoMapper cargoMapper;


  private final DocumentoEntityRepository documentoEntityRepository;
  private final DocumentoMapper documentoMapper;

  @Transactional
  @Override
  public Contrato save(Contrato contrato) {

    var entity = contratoMapper.toEntity(contrato);
    var saved = contratoEntityRepository.save(entity);

     DocumentoEntity documentoEntity = null;

    if(contrato.getContratoAnexo() != null) {
      System.out.println("repo:: "+contrato.getContratoAnexo().getTipoDocumento());
      documentoEntity = documentoMapper.toEntity(contrato.getContratoAnexo());
      documentoEntityRepository.save(documentoEntity);
    }

    return contratoMapper.toDomain(saved, documentoEntity);
  }


  @Transactional(readOnly = true)
  @Override
  public Optional<Contrato> getById(ExternalID contratoId) {
    var contratoOpt = contratoEntityRepository.findById(contratoId.getValor());

    if (contratoOpt.isEmpty()) return Optional.empty();

    var contratoEntity = contratoOpt.get();

    var documentoOpt = documentoEntityRepository.findFirstByObjectIdAndObjectoTipo(
        contratoEntity.getId(), ObjetoTipo.CONTRATO
    );

    return Optional.of(
        contratoMapper.toDomain(contratoEntity, documentoOpt.orElse(null))
    );
  }

  @Transactional(readOnly = true)
  @Override
  public List<Contrato> getAll() {
    return contratoEntityRepository.findAllByEstado(Estado.A)
        .stream()
        .map(contratoMapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Contrato> getAllByFuncionariolId(ExternalID funcionarioId) {
    return contratoEntityRepository.findByIdFuncionario_Id(funcionarioId.getValor())
        .stream()
        .map(contratoMapper::toDomain)
        .toList();
  }
}

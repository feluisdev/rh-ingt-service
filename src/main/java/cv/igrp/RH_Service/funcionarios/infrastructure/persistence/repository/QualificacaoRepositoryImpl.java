package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.QualificacaoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Qualificacao;
import cv.igrp.RH_Service.funcionarios.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.QualificacaoMapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.application.constants.ObjetoTipo;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DocumentoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.QualificacaoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.DocumentoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.QualificacaoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QualificacaoRepositoryImpl implements QualificacaoRepository {

  private final QualificacaoEntityRepository qualificacaoJpaRepository;
  private final QualificacaoMapper qualificacaoMapper;

  private final DocumentoEntityRepository documentoEntityRepository;
  private final DocumentoMapper documentoMapper;


  @Transactional
  @Override
  public Qualificacao save(Qualificacao qualificacao) {

    var entity = qualificacaoMapper.toEntity(qualificacao);
    var saved = qualificacaoJpaRepository.save(entity);

    DocumentoEntity documentoEntity = null;

    if(qualificacao.getDocumento() != null) {
      System.out.println("repo:: "+qualificacao.getDocumento() .getTipoDocumento().getDescricao());
      documentoEntity = documentoMapper.toEntity(qualificacao.getDocumento() );
      documentoEntityRepository.save(documentoEntity);
    }

    return qualificacaoMapper.toDomain(saved, documentoEntity);
  }


  @Transactional(readOnly = true)
  @Override
  public Optional<Qualificacao> getById(ExternalID externalId) {
    var qualificacaoOpt = qualificacaoJpaRepository.findById(externalId.getValor());

    if (qualificacaoOpt.isEmpty()) return Optional.empty();

    var qualificacaoEntity = qualificacaoOpt.get();

    var documentoOpt = documentoEntityRepository.findFirstByObjectIdAndObjectoTipo(
        qualificacaoEntity.getId(), ObjetoTipo.QUALIFICACAO
    );

    return Optional.of(
        qualificacaoMapper.toDomain(qualificacaoEntity, documentoOpt.orElse(null))
    );
  }


  @Transactional(readOnly = true)
  @Override
  public List<Qualificacao> getAll() {
    List<QualificacaoEntity> list = qualificacaoJpaRepository.findAllByEstado(Estado.A);

    return list.stream()
        .map(qualificacaoMapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Qualificacao> getAll(QualificacaoFilter filter) {
    return List.of();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Qualificacao> getAllByFuncionarioId(ExternalID funcionarioExternalId) {
    var list = qualificacaoJpaRepository.findAllByIdFuncionario_Id_AndEstado(funcionarioExternalId.getValor(), Estado.A);
    return list.stream()
        .map(qualificacaoMapper::toDomain)
        .toList();
  }
}

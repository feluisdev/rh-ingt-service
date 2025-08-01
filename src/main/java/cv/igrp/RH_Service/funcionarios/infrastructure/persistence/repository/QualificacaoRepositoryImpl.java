package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.QualificacaoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Qualificacao;
import cv.igrp.RH_Service.funcionarios.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.QualificacaoMapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.QualificacaoEntity;
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
  private final FuncionarioMapper funcionarioMapper;


  @Transactional
  @Override
  public Qualificacao save(Qualificacao qualificacao) {

    var funcionarioEntity = funcionarioMapper.toEntity(qualificacao.getFuncionario());

    var entity = qualificacaoMapper.toEntity(qualificacao, funcionarioEntity);
    var saved = qualificacaoJpaRepository.save(entity);

    var funcionarioDomain = funcionarioMapper.toLightDomain(funcionarioEntity);


    return qualificacaoMapper.toDomainWithFuncionario(saved, funcionarioDomain);
  }


  @Transactional(readOnly = true)
  @Override
  public Optional<Qualificacao> getByExternalId(ExternalID externalId) {
    return qualificacaoJpaRepository.findByExternalId(externalId.getValor())
        .map(entity -> {
          var funcionarioDomain = funcionarioMapper.toLightDomain(entity.getIdFuncionario());
          return qualificacaoMapper.toDomainWithFuncionario(entity, funcionarioDomain);
        });
  }

  @Transactional(readOnly = true)
  @Override
  public List<Qualificacao> getAll() {
    List<QualificacaoEntity> list = qualificacaoJpaRepository.findAllByEstado(Estado.A);

    return list.stream()
        .map(entity -> {
          var funcionarioDomain = funcionarioMapper.toLightDomain(entity.getIdFuncionario());
          return qualificacaoMapper.toDomainWithFuncionario(entity, funcionarioDomain);
        })
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Qualificacao> getAll(QualificacaoFilter filter) {
    return List.of();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Qualificacao> getAllByFuncionarioExternalId(ExternalID funcionarioExternalId) {
    var list = qualificacaoJpaRepository.findAllByIdFuncionario_ExternalId_AndEstado(funcionarioExternalId.getValor(), Estado.A);
    return list.stream()
        .map(entity -> {
          var funcionarioDomain = funcionarioMapper.toLightDomain(entity.getIdFuncionario());
          return qualificacaoMapper.toDomainWithFuncionario(entity, funcionarioDomain);
        })
        .toList();
  }
}

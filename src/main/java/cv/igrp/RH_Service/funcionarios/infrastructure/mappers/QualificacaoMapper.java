package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.application.dto.QualificacaoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Funcionario;
import cv.igrp.RH_Service.funcionarios.domain.models.Qualificacao;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.QualificacaoEntity;
import org.springframework.stereotype.Component;

@Component
public class QualificacaoMapper {

  public Qualificacao toDomainWithFuncionario(QualificacaoEntity entity, Funcionario funcionarioDomain) {
    if (entity == null) {
      return null;
    }

    return Qualificacao.reconstruir(
        entity.getId(),
        ExternalID.from(entity.getExternalId()),
        entity.getInstituicao(),
        entity.getCurso(),
        entity.getDataInicio(),
        entity.getDataConclusao(),
        entity.getNivel(),
        entity.getSituacao(),
        entity.getCargaHoraria(),
        entity.getNotaFinal(),
        entity.getEstado(),
        funcionarioDomain
    );
  }


  public QualificacaoEntity toEntity(Qualificacao domain, FuncionarioEntity funcionarioEntity) {
    if (domain == null) {
      return null;
    }

    QualificacaoEntity entity = new QualificacaoEntity();

    entity.setId(domain.getId());
    entity.setExternalId(domain.getExternalId().getValor());
    entity.setInstituicao(domain.getInstituicao());
    entity.setCurso(domain.getCurso());
    entity.setDataInicio(domain.getDataInicio());
    entity.setDataConclusao(domain.getDataConclusao());
    entity.setNivel(domain.getNivel());
    entity.setSituacao(domain.getSituacao());
    entity.setCargaHoraria(domain.getCargaHoraria());
    entity.setNotaFinal(domain.getNotaFinal());
    entity.setEstado(domain.getEstado());
    entity.setIdFuncionario(funcionarioEntity);

    return entity;
  }

  public QualificacaoResponseDTO toDTO(Qualificacao qualificacao) {
    if (qualificacao == null) {
      return null;
    }

    QualificacaoResponseDTO dto = new QualificacaoResponseDTO();

    dto.setQualificacaoId(qualificacao.getExternalId().getStringValor());
    dto.setInstituicao(qualificacao.getInstituicao());
    dto.setCurso(qualificacao.getCurso());
    dto.setDataInicio(qualificacao.getDataInicio());
    dto.setDataConclusao(qualificacao.getDataConclusao());
    dto.setNivel(qualificacao.getNivel());
    dto.setSituacao(qualificacao.getSituacao());
    dto.setCargaHoraria(qualificacao.getCargaHoraria());
    dto.setNotaFinal(qualificacao.getNotaFinal());
    dto.setEstado(qualificacao.getEstado().getCode());
    dto.setEstadoDesc(qualificacao.getEstado().getDescription());

    return dto;
  }


}

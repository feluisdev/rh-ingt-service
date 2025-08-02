package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.application.dto.QualificacaoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Documento;
import cv.igrp.RH_Service.funcionarios.domain.models.Funcionario;
import cv.igrp.RH_Service.funcionarios.domain.models.Qualificacao;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DocumentoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.QualificacaoEntity;
import org.springframework.stereotype.Component;

@Component
public class QualificacaoMapper {

  private final DocumentoMapper documentoMapper;

  public QualificacaoMapper(DocumentoMapper documentoMapper) {
    this.documentoMapper = documentoMapper;
  }

  public QualificacaoEntity toEntity(Qualificacao domain) {
    if (domain == null) {
      return null;
    }

    QualificacaoEntity entity = new QualificacaoEntity();
    entity.setId(domain.getIdQualificacao().getValor());
    entity.setInstituicao(domain.getInstituicao());
    entity.setCurso(domain.getCurso());
    entity.setDataInicio(domain.getDataInicio());
    entity.setDataConclusao(domain.getDataConclusao());
    entity.setNivel(domain.getNivel());
    entity.setSituacao(domain.getSituacao());
    entity.setCargaHoraria(domain.getCargaHoraria());
    entity.setNotaFinal(domain.getNotaFinal());
    entity.setEstado(domain.getEstado());

    FuncionarioEntity funcionarioEntity = new FuncionarioEntity();
    funcionarioEntity.setId(domain.getFuncionarioId().getValor());
    entity.setIdFuncionario(funcionarioEntity);

    return entity;
  }

  public Qualificacao toDomain(QualificacaoEntity entity) {
    if (entity == null) {
      return null;
    }

    return Qualificacao.reconstruir(
        ExternalID.from(entity.getId()),
        entity.getInstituicao(),
        entity.getCurso(),
        entity.getDataInicio(),
        entity.getDataConclusao(),
        entity.getNivel(),
        entity.getSituacao(),
        entity.getCargaHoraria(),
        entity.getNotaFinal(),
        entity.getEstado(),
        ExternalID.from(entity.getIdFuncionario().getId())
    );
  }

  public Qualificacao toDomain(QualificacaoEntity entity, DocumentoEntity documentoEntity) {
    if (entity == null) {
      return null;
    }

    Documento documentoQualificao = documentoEntity != null
        ? documentoMapper.toDomain(documentoEntity)
        : null;

    return Qualificacao.reconstruir(
        ExternalID.from(entity.getId()),
        entity.getInstituicao(),
        entity.getCurso(),
        entity.getDataInicio(),
        entity.getDataConclusao(),
        entity.getNivel(),
        entity.getSituacao(),
        entity.getCargaHoraria(),
        entity.getNotaFinal(),
        entity.getEstado(),
        ExternalID.from(entity.getIdFuncionario().getId()),
        documentoQualificao
    );
  }

  public QualificacaoResponseDTO toDTO(Qualificacao qualificacao) {
    if (qualificacao == null) {
      return null;
    }

    QualificacaoResponseDTO dto = new QualificacaoResponseDTO();

    dto.setQualificacaoId(qualificacao.getIdQualificacao().getStringValor());
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

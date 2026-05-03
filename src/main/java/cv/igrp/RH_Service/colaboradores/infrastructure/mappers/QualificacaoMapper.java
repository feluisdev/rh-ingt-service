package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.QualificacaoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Qualificacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.QualificacaoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.QualificacaoEntity;
import org.springframework.stereotype.Component;

@Component("colabsQualificacaoMapper")
public class QualificacaoMapper {

    public Qualificacao toDomain(QualificacaoEntity e) {
        return Qualificacao.reconstituir(
                QualificacaoId.from(e.getId()),
                FuncionarioId.from(e.getFuncionarioId()),
                e.getNivelAcademico(), e.getCurso(), e.getInstituicao(),
                e.getAnoConclusao(), e.getPais(), e.getIsActive());
    }

    public QualificacaoEntity toEntity(Qualificacao q) {
        QualificacaoEntity e = new QualificacaoEntity();
        e.setId(q.getId().getValor());
        e.setFuncionarioId(q.getFuncionarioId().getValor());
        e.setNivelAcademico(q.getNivelAcademico());
        e.setCurso(q.getCurso());
        e.setInstituicao(q.getInstituicao());
        e.setAnoConclusao(q.getAnoConclusao());
        e.setPais(q.getPais());
        e.setIsActive(q.getIsActive());
        return e;
    }

    public QualificacaoResponseDTO toDTO(Qualificacao q) {
        QualificacaoResponseDTO r = new QualificacaoResponseDTO();
        r.setId(q.getId().getStringValor());
        r.setFuncionarioId(q.getFuncionarioId().getStringValor());
        r.setNivelAcademico(q.getNivelAcademico());
        r.setCurso(q.getCurso());
        r.setInstituicao(q.getInstituicao());
        r.setAnoConclusao(q.getAnoConclusao());
        r.setPais(q.getPais());
        r.setIsActive(q.getIsActive());
        return r;
    }
}

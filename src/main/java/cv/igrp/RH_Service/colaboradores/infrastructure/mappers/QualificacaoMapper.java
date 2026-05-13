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
                e.getLevel(), e.getCourseName(), e.getInstitution(),
                e.getCountry(), e.getStartDate(), e.getEndDate(),
                e.getCompleted(), e.getIsActive());
    }

    public QualificacaoEntity toEntity(Qualificacao q) {
        QualificacaoEntity e = new QualificacaoEntity();
        e.setId(q.getId().getValor());
        e.setFuncionarioId(q.getFuncionarioId().getValor());
        e.setLevel(q.getLevel());
        e.setCourseName(q.getCourseName());
        e.setInstitution(q.getInstitution());
        e.setCountry(q.getCountry());
        e.setStartDate(q.getStartDate());
        e.setEndDate(q.getEndDate());
        e.setCompleted(q.getCompleted());
        e.setIsActive(q.getIsActive());
        return e;
    }

    public QualificacaoResponseDTO toDTO(Qualificacao q) {
        QualificacaoResponseDTO r = new QualificacaoResponseDTO();
        r.setId(q.getId().getStringValor());
        r.setFuncionarioId(q.getFuncionarioId().getStringValor());
        r.setLevel(q.getLevel());
        r.setCourseName(q.getCourseName());
        r.setInstitution(q.getInstitution());
        r.setCountry(q.getCountry());
        r.setStartDate(q.getStartDate());
        r.setEndDate(q.getEndDate());
        r.setCompleted(q.getCompleted());
        r.setCompletedDesc(Boolean.TRUE.equals(q.getCompleted()) ? "Concluída" : "Em curso");
        r.setIsActive(q.getIsActive());
        r.setEstadoDesc(Boolean.TRUE.equals(q.getIsActive()) ? "Ativo" : "Inativo");
        return r;
    }
}

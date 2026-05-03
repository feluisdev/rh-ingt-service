package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.ColocacaoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Colocacao;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ColocacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ColocacaoEntity;
import org.springframework.stereotype.Component;

@Component("colabsColocacaoMapper")
public class ColocacaoMapper {

    public Colocacao toDomain(ColocacaoEntity e) {
        return Colocacao.reconstituir(
                ColocacaoId.from(e.getId()),
                FuncionarioId.from(e.getFuncionarioId()),
                e.getUnitId(),
                e.getJobId(),
                e.getStartDate(),
                e.getEndDate(),
                e.getIsCurrent(),
                e.getIsActive(),
                TipoAfectacao.valueOf(e.getAssignmentType()),
                e.getNotes());
    }

    public ColocacaoEntity toEntity(Colocacao c) {
        ColocacaoEntity e = new ColocacaoEntity();
        e.setId(c.getId().getValor());
        e.setFuncionarioId(c.getFuncionarioId().getValor());
        e.setUnitId(c.getUnitId());
        e.setJobId(c.getJobId());
        e.setStartDate(c.getStartDate());
        e.setEndDate(c.getEndDate());
        e.setIsCurrent(c.getIsCurrent());
        e.setIsActive(c.getIsActive());
        e.setAssignmentType(c.getAssignmentType().name());
        e.setNotes(c.getNotes());
        return e;
    }

    public ColocacaoResponseDTO toDTO(Colocacao c) {
        ColocacaoResponseDTO r = new ColocacaoResponseDTO();
        r.setId(c.getId().getStringValor());
        r.setFuncionarioId(c.getFuncionarioId().getStringValor());
        r.setUnitId(c.getUnitId() != null ? c.getUnitId().toString() : null);
        r.setJobId(c.getJobId() != null ? c.getJobId().toString() : null);
        r.setStartDate(c.getStartDate());
        r.setEndDate(c.getEndDate());
        r.setIsCurrent(c.getIsCurrent());
        r.setIsActive(c.getIsActive());
        r.setAssignmentType(c.getAssignmentType().name());
        r.setNotes(c.getNotes());
        return r;
    }
}

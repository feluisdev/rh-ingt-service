package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.AssignmentEntity;
import org.springframework.stereotype.Component;

@Component
public class AssignmentMapper {

    public Assignment toDomain(AssignmentEntity e) {
        if (e == null) return null;
        return Assignment.reconstituir(
                AssignmentId.from(e.getId()),
                FuncionarioId.from(e.getFuncionarioId()),
                e.getPositionId(),
                e.getGradeId(),
                e.getFunctionId(),
                e.getAssignmentType(),
                e.getOrigem(),
                e.getOriginAssignmentId(),
                e.getDataInicio(),
                e.getDataFim(),
                e.getIsCurrent(),
                e.getIsActive(),
                e.getNotes()
        );
    }

    public AssignmentEntity toEntity(Assignment a) {
        if (a == null) return null;
        AssignmentEntity entity = new AssignmentEntity();
        entity.setId(a.getId().getValor());
        entity.setFuncionarioId(a.getFuncionarioId().getValor());
        entity.setPositionId(a.getPositionId());
        entity.setGradeId(a.getGradeId());
        entity.setFunctionId(a.getFunctionId());
        entity.setAssignmentType(a.getAssignmentType());
        entity.setOrigem(a.getOrigem());
        entity.setOriginAssignmentId(a.getOriginAssignmentId());
        entity.setDataInicio(a.getDataInicio());
        entity.setDataFim(a.getDataFim());
        entity.setIsCurrent(a.getIsCurrent());
        entity.setIsActive(a.getIsActive());
        entity.setNotes(a.getNotes());
        return entity;
    }
}

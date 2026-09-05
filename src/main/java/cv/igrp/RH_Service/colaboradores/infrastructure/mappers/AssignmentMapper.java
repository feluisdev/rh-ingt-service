package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.GradeEntity;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.AssignmentEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.FunctionEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.PositionEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssignmentMapper {

    private final JpaReferences refs;

    public Assignment toDomain(AssignmentEntity e) {
        if (e == null) return null;
        return Assignment.reconstituir(
                AssignmentId.from(e.getId()),
                FuncionarioId.from(e.getFuncionario().getId()),
                e.getPosition().getId(),
                refs.idOf(e.getGrade(), GradeEntity::getId),
                refs.idOf(e.getFunction(), FunctionEntity::getId),
                e.getAssignmentType(),
                e.getOrigem(),
                refs.idOf(e.getOriginAssignment(), AssignmentEntity::getId),
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
        entity.setFuncionario(refs.ref(FuncionarioEntity.class, a.getFuncionarioId().getValor()));
        entity.setPosition(refs.ref(PositionEntity.class, a.getPositionId()));
        entity.setGrade(refs.ref(GradeEntity.class, a.getGradeId()));
        entity.setFunction(refs.ref(FunctionEntity.class, a.getFunctionId()));
        entity.setAssignmentType(a.getAssignmentType());
        entity.setOrigem(a.getOrigem());
        entity.setOriginAssignment(refs.ref(AssignmentEntity.class, a.getOriginAssignmentId()));
        entity.setDataInicio(a.getDataInicio());
        entity.setDataFim(a.getDataFim());
        entity.setIsCurrent(a.getIsCurrent());
        entity.setIsActive(a.getIsActive());
        entity.setNotes(a.getNotes());
        return entity;
    }
}

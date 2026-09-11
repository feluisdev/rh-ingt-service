package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.ProcessoDisciplinarDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoDisciplinarId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ProcessoDisciplinarEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("colabsProcessoDisciplinarMapper")
@RequiredArgsConstructor
public class ProcessoDisciplinarMapper {

    private final JpaReferences refs;

    public ProcessoDisciplinar toDomain(ProcessoDisciplinarEntity e) {
        return ProcessoDisciplinar.reconstituir(
                ProcessoDisciplinarId.from(e.getId()),
                FuncionarioId.from(e.getFuncionario().getId()),
                e.getProcessNumber(), e.getStartDate(), e.getEndDate(),
                e.getPenalty(), e.getPenaltyStartDate(), e.getPenaltyEndDate(),
                e.getOfficialBulletin(), e.getNotes());
    }

    public ProcessoDisciplinarEntity toEntity(ProcessoDisciplinar p) {
        ProcessoDisciplinarEntity e = new ProcessoDisciplinarEntity();
        e.setId(p.getId().getValor());
        e.setFuncionario(refs.ref(FuncionarioEntity.class, p.getFuncionarioId().getValor()));
        e.setProcessNumber(p.getProcessNumber());
        e.setStartDate(p.getStartDate());
        e.setEndDate(p.getEndDate());
        e.setPenalty(p.getPenalty());
        e.setPenaltyStartDate(p.getPenaltyStartDate());
        e.setPenaltyEndDate(p.getPenaltyEndDate());
        e.setOfficialBulletin(p.getOfficialBulletin());
        e.setNotes(p.getNotes());
        return e;
    }

    public ProcessoDisciplinarDTO toDTO(ProcessoDisciplinar p) {
        ProcessoDisciplinarDTO dto = new ProcessoDisciplinarDTO();
        dto.setId(p.getId().getStringValor());
        dto.setFuncionarioId(p.getFuncionarioId().getStringValor());
        dto.setProcessNumber(p.getProcessNumber());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setPenalty(p.getPenalty());
        dto.setPenaltyStartDate(p.getPenaltyStartDate());
        dto.setPenaltyEndDate(p.getPenaltyEndDate());
        dto.setOfficialBulletin(p.getOfficialBulletin());
        dto.setNotes(p.getNotes());
        return dto;
    }
}

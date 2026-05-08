package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.FormacaoDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Formacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FormacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FormacaoEntity;
import org.springframework.stereotype.Component;

@Component("colabsFormacaoMapper")
public class FormacaoMapper {

    public Formacao toDomain(FormacaoEntity e) {
        return Formacao.reconstituir(
                FormacaoId.from(e.getId()),
                FuncionarioId.from(e.getFuncionarioId()),
                e.getName(), e.getInstitution(), e.getTrainingType(),
                e.getStartDate(), e.getEndDate(), e.getDurationHours());
    }

    public FormacaoEntity toEntity(Formacao f) {
        FormacaoEntity e = new FormacaoEntity();
        e.setId(f.getId().getValor());
        e.setFuncionarioId(f.getFuncionarioId().getValor());
        e.setName(f.getName());
        e.setInstitution(f.getInstitution());
        e.setTrainingType(f.getTrainingType());
        e.setStartDate(f.getStartDate());
        e.setEndDate(f.getEndDate());
        e.setDurationHours(f.getDurationHours());
        return e;
    }

    public FormacaoDTO toDTO(Formacao f) {
        FormacaoDTO dto = new FormacaoDTO();
        dto.setId(f.getId().getStringValor());
        dto.setFuncionarioId(f.getFuncionarioId().getStringValor());
        dto.setName(f.getName());
        dto.setInstitution(f.getInstitution());
        dto.setTrainingType(f.getTrainingType());
        dto.setStartDate(f.getStartDate());
        dto.setEndDate(f.getEndDate());
        dto.setDurationHours(f.getDurationHours());
        return dto;
    }
}

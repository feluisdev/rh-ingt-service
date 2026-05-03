package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.EnquadramentoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.EnquadramentoProfissional;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.EnquadramentoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.EnquadramentoEntity;
import org.springframework.stereotype.Component;

@Component
public class EnquadramentoMapper {

    public EnquadramentoProfissional toDomain(EnquadramentoEntity e) {
        return EnquadramentoProfissional.reconstituir(
                EnquadramentoId.from(e.getId()),
                FuncionarioId.from(e.getFuncionarioId()),
                e.getCareerId(),
                e.getCategoryId(),
                e.getGradeId(),
                e.getCargoId(),
                e.getUnidadeOrganicaId(),
                e.getDataInicio(),
                e.getDataFim(),
                e.getIsCurrent()
        );
    }

    public EnquadramentoEntity toEntity(EnquadramentoProfissional e) {
        EnquadramentoEntity entity = new EnquadramentoEntity();
        entity.setId(e.getId().getValor());
        entity.setFuncionarioId(e.getFuncionarioId().getValor());
        entity.setCareerId(e.getCareerId());
        entity.setCategoryId(e.getCategoryId());
        entity.setGradeId(e.getGradeId());
        entity.setCargoId(e.getCargoId());
        entity.setUnidadeOrganicaId(e.getUnidadeOrganicaId());
        entity.setDataInicio(e.getDataInicio());
        entity.setDataFim(e.getDataFim());
        entity.setIsCurrent(e.getIsCurrent());
        return entity;
    }

    public EnquadramentoResponseDTO toDTO(EnquadramentoProfissional e) {
        EnquadramentoResponseDTO r = new EnquadramentoResponseDTO();
        r.setId(e.getId().getStringValor());
        r.setFuncionarioId(e.getFuncionarioId().getStringValor());
        r.setCareerId(e.getCareerId().toString());
        r.setCategoryId(e.getCategoryId().toString());
        r.setGradeId(e.getGradeId().toString());
        r.setCargoId(e.getCargoId().toString());
        r.setUnidadeOrganicaId(e.getUnidadeOrganicaId().toString());
        r.setDataInicio(e.getDataInicio());
        r.setDataFim(e.getDataFim());
        r.setIsCurrent(e.getIsCurrent());
        return r;
    }
}

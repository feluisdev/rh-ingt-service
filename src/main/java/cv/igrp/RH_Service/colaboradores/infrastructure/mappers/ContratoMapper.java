package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.ContratoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ContratoEntity;
import org.springframework.stereotype.Component;

@Component("colabsContratoMapper")
public class ContratoMapper {

    public Contrato toDomain(ContratoEntity e) {
        return Contrato.reconstituir(
                ContratoId.from(e.getId()),
                FuncionarioId.from(e.getFuncionarioId()),
                e.getContractTypeId(),
                e.getContractNumber(),
                e.getStartDate(),
                e.getEndDate(),
                e.getTerminationReason(),
                e.getIsCurrent(),
                e.getLegalBase(),
                e.getNotes());
    }

    public ContratoEntity toEntity(Contrato c) {
        ContratoEntity e = new ContratoEntity();
        e.setId(c.getId().getValor());
        e.setFuncionarioId(c.getFuncionarioId().getValor());
        e.setContractTypeId(c.getContractTypeId());
        e.setContractNumber(c.getContractNumber());
        e.setStartDate(c.getStartDate());
        e.setEndDate(c.getEndDate());
        e.setTerminationReason(c.getTerminationReason());
        e.setIsCurrent(c.getIsCurrent());
        e.setLegalBase(c.getLegalBase());
        e.setNotes(c.getNotes());
        return e;
    }

    public ContratoResponseDTO toDTO(Contrato c) {
        ContratoResponseDTO r = new ContratoResponseDTO();
        r.setId(c.getId().getStringValor());
        r.setFuncionarioId(c.getFuncionarioId().getStringValor());
        r.setContractTypeId(c.getContractTypeId() != null ? c.getContractTypeId().toString() : null);
        r.setContractNumber(c.getContractNumber());
        r.setStartDate(c.getStartDate());
        r.setEndDate(c.getEndDate());
        r.setTerminationReason(c.getTerminationReason());
        r.setIsCurrent(c.getIsCurrent());
        r.setLegalBase(c.getLegalBase());
        r.setNotes(c.getNotes());
        return r;
    }
}

package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.ContratoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ContratoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.ContractTypeEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("colabsContratoMapper")
@RequiredArgsConstructor
public class ContratoMapper {

    private final JpaReferences refs;

    public Contrato toDomain(ContratoEntity e) {
        return Contrato.reconstituir(
                ContratoId.from(e.getId()),
                FuncionarioId.from(e.getFuncionario().getId()),
                refs.idOf(e.getContractType(), ContractTypeEntity::getId),
                e.getContractNumber(),
                e.getStartDate(),
                e.getEndDate(),
                e.getTerminationReason(),
                e.getIsCurrent(),
                e.getStatus(),
                e.getRenewalCount(),
                e.getRegimeTrabalho(),
                e.getPercentagemTempo(),
                e.getLegalBase(),
                e.getNotes());
    }

    public ContratoEntity toEntity(Contrato c) {
        ContratoEntity e = new ContratoEntity();
        e.setId(c.getId().getValor());
        e.setFuncionario(refs.ref(FuncionarioEntity.class, c.getFuncionarioId().getValor()));
        e.setContractType(refs.ref(ContractTypeEntity.class, c.getContractTypeId()));
        e.setContractNumber(c.getContractNumber());
        e.setStartDate(c.getStartDate());
        e.setEndDate(c.getEndDate());
        e.setTerminationReason(c.getTerminationReason());
        e.setIsCurrent(c.getIsCurrent());
        e.setStatus(c.getStatus());
        e.setRenewalCount(c.getRenewalCount());
        e.setRegimeTrabalho(c.getRegimeTrabalho());
        e.setPercentagemTempo(c.getPercentagemTempo());
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
        r.setIsCurrentDesc(Boolean.TRUE.equals(c.getIsCurrent()) ? "Atual" : "Anterior");
        r.setStatus(c.getStatus());
        r.setRenewalCount(c.getRenewalCount());
        r.setRegimeTrabalho(c.getRegimeTrabalho());
        r.setPercentagemTempo(c.getPercentagemTempo());
        r.setLegalBase(c.getLegalBase());
        r.setNotes(c.getNotes());
        return r;
    }
}

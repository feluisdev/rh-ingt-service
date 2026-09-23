package cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers;

import cv.igrp.RH_Service.parametrizacoes.application.dto.LeaveTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveType;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContagemDias;
import cv.igrp.RH_Service.parametrizacoes.domain.models.EfeitoNaRemuneracao;
import cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.LeaveTypeEntity;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveTypeId;
import org.springframework.stereotype.Component;

@Component
public class LeaveTypeMapper {

    public LeaveTypeEntity toEntity(LeaveType domain) {
        if (domain == null) return null;
        LeaveTypeEntity entity = new LeaveTypeEntity();
        entity.setId(domain.getId().getValor());
        entity.setCode(domain.getCode());
        entity.setDescription(domain.getDescription());
        entity.setDeductsBalance(domain.isDeductsBalance());
        entity.setRequiresApproval(domain.isRequiresApproval());
        entity.setMaxDaysPerYear(domain.getMaxDaysPerYear());
        entity.setMaxDaysPerOccurrence(domain.getMaxDaysPerOccurrence());
        entity.setMaxDaysPerMonth(domain.getMaxDaysPerMonth());
        entity.setCategory(domain.getCategory());
        entity.setRegime(domain.getRegime() != null ? domain.getRegime().name() : RegimeAusencia.FALTA.name());
        entity.setEfeitoRemuneracao(domain.getEfeitoRemuneracao() != null
                ? domain.getEfeitoRemuneracao().name() : EfeitoNaRemuneracao.SEM_PERDA.name());
        entity.setContagem(domain.getContagem() != null
                ? domain.getContagem().name() : ContagemDias.DIAS_UTEIS.name());
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public LeaveType toDomain(LeaveTypeEntity entity) {
        if (entity == null) return null;
        return LeaveType.reconstruir(
            LeaveTypeId.from(entity.getId()),
            entity.getCode(),
            entity.getDescription(),
            entity.getDeductsBalance() != null && entity.getDeductsBalance(),
            entity.getRequiresApproval() != null && entity.getRequiresApproval(),
            entity.getMaxDaysPerYear(),
            entity.getMaxDaysPerOccurrence(),
            entity.getMaxDaysPerMonth(),
            entity.getCategory(),
            entity.getIsActive() != null && entity.getIsActive(),
            RegimeAusencia.de(entity.getRegime()),
            EfeitoNaRemuneracao.de(entity.getEfeitoRemuneracao()),
            ContagemDias.de(entity.getContagem())
        );
    }

    public LeaveTypeResponseDTO toDTO(LeaveType domain) {
        if (domain == null) return null;
        var dto = new LeaveTypeResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setDescription(domain.getDescription());
        dto.setDeductsBalance(domain.isDeductsBalance());
        dto.setRequiresApproval(domain.isRequiresApproval());
        dto.setMaxDaysPerYear(domain.getMaxDaysPerYear());
        dto.setMaxDaysPerOccurrence(domain.getMaxDaysPerOccurrence());
        dto.setMaxDaysPerMonth(domain.getMaxDaysPerMonth());
        dto.setCategory(domain.getCategory());
        dto.setRegime(domain.getRegime() != null ? domain.getRegime().name() : null);
        dto.setEfeitoRemuneracao(domain.getEfeitoRemuneracao() != null
                ? domain.getEfeitoRemuneracao().name() : null);
        dto.setContagem(domain.getContagem() != null ? domain.getContagem().name() : null);
        dto.setIsActive(domain.isActive());
        dto.setEstadoDesc(Boolean.TRUE.equals(domain.isActive()) ? "Ativo" : "Inativo");
        return dto;
    }
}

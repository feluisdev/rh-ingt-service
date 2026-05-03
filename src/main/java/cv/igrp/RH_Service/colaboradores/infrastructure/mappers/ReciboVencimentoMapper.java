package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.ReciboVencimentoDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.ReciboVencimento;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ReciboVencimentoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ReciboVencimentoEntity;
import org.springframework.stereotype.Component;

@Component("colabsReciboVencimentoMapper")
public class ReciboVencimentoMapper {

    public ReciboVencimento toDomain(ReciboVencimentoEntity e) {
        return ReciboVencimento.reconstituir(
                ReciboVencimentoId.from(e.getId()),
                FuncionarioId.from(e.getFuncionarioId()),
                e.getPeriodMonth(), e.getPeriodYear(), e.getIssueDate(),
                e.getGrossSalary(), e.getNetSalary(), e.getDocumentId());
    }

    public ReciboVencimentoEntity toEntity(ReciboVencimento r) {
        ReciboVencimentoEntity e = new ReciboVencimentoEntity();
        e.setId(r.getId().getValor());
        e.setFuncionarioId(r.getFuncionarioId().getValor());
        e.setPeriodMonth(r.getPeriodMonth());
        e.setPeriodYear(r.getPeriodYear());
        e.setIssueDate(r.getIssueDate());
        e.setGrossSalary(r.getGrossSalary());
        e.setNetSalary(r.getNetSalary());
        e.setDocumentId(r.getDocumentId());
        return e;
    }

    public ReciboVencimentoDTO toDTO(ReciboVencimento r) {
        ReciboVencimentoDTO dto = new ReciboVencimentoDTO();
        dto.setId(r.getId().getStringValor());
        dto.setFuncionarioId(r.getFuncionarioId().getStringValor());
        dto.setPeriodMonth(r.getPeriodMonth());
        dto.setPeriodYear(r.getPeriodYear());
        dto.setIssueDate(r.getIssueDate());
        dto.setGrossSalary(r.getGrossSalary());
        dto.setNetSalary(r.getNetSalary());
        dto.setDocumentId(r.getDocumentId());
        return dto;
    }
}

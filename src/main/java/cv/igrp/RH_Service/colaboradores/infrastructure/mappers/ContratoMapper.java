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
                e.getTipoContrato(), e.getDataInicio(), e.getDataFim(),
                e.getNumeroContrato(), e.getIsActive());
    }

    public ContratoEntity toEntity(Contrato c) {
        ContratoEntity e = new ContratoEntity();
        e.setId(c.getId().getValor());
        e.setFuncionarioId(c.getFuncionarioId().getValor());
        e.setTipoContrato(c.getTipoContrato());
        e.setDataInicio(c.getDataInicio());
        e.setDataFim(c.getDataFim());
        e.setNumeroContrato(c.getNumeroContrato());
        e.setIsActive(c.getIsActive());
        return e;
    }

    public ContratoResponseDTO toDTO(Contrato c) {
        ContratoResponseDTO r = new ContratoResponseDTO();
        r.setId(c.getId().getStringValor());
        r.setFuncionarioId(c.getFuncionarioId().getStringValor());
        r.setTipoContrato(c.getTipoContrato());
        r.setDataInicio(c.getDataInicio());
        r.setDataFim(c.getDataFim());
        r.setNumeroContrato(c.getNumeroContrato());
        r.setIsActive(c.getIsActive());
        return r;
    }
}

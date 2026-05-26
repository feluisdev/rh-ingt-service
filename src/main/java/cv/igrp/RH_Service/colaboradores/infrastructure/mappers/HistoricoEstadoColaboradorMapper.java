package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.HistoricoEstadoColaboradorResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.HistoricoEstadoColaboradorId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.HistoricoEstadoColaboradorEntity;
import org.springframework.stereotype.Component;

@Component
public class HistoricoEstadoColaboradorMapper {

    public HistoricoEstadoColaboradorEntity toEntity(HistoricoEstadoColaborador domain) {
        HistoricoEstadoColaboradorEntity e = new HistoricoEstadoColaboradorEntity();
        e.setId(domain.getId().getValor());
        e.setFuncionarioId(domain.getFuncionarioId().getValor());
        e.setEstadoAnteriorId(domain.getEstadoAnteriorId());
        e.setEstadoNovoId(domain.getEstadoNovoId());
        e.setMotivoCkey(domain.getMotivoCkey());
        e.setDataEfectividade(domain.getDataEfectividade());
        e.setObservacao(domain.getObservacao());
        return e;
    }

    public HistoricoEstadoColaborador toDomain(HistoricoEstadoColaboradorEntity e) {
        return HistoricoEstadoColaborador.reconstruir(
                HistoricoEstadoColaboradorId.from(e.getId()),
                FuncionarioId.from(e.getFuncionarioId()),
                e.getEstadoAnteriorId(),
                e.getEstadoNovoId(),
                e.getMotivoCkey(),
                e.getDataEfectividade(),
                e.getObservacao(),
                e.getCreatedBy(),
                e.getCreatedDate()
        );
    }

    public HistoricoEstadoColaboradorResponseDTO toDTO(HistoricoEstadoColaborador domain) {
        HistoricoEstadoColaboradorResponseDTO dto = new HistoricoEstadoColaboradorResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setFuncionarioId(domain.getFuncionarioId().getStringValor());
        dto.setEstadoAnteriorId(domain.getEstadoAnteriorId() != null ? domain.getEstadoAnteriorId().toString() : null);
        dto.setEstadoNovoId(domain.getEstadoNovoId().toString());
        dto.setMotivoCkey(domain.getMotivoCkey());
        dto.setDataEfectividade(domain.getDataEfectividade());
        dto.setObservacao(domain.getObservacao());
        dto.setRegistadoPor(domain.getRegistadoPor());
        dto.setRegistadoEm(domain.getRegistadoEm());
        return dto;
    }
}

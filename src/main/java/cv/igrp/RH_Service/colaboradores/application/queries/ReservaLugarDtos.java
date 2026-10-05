package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ReservaLugarResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.ReservaLugar;
import cv.igrp.RH_Service.estrutura.domain.models.Position;

import java.util.UUID;

/** Reserva de Lugar → DTO. */
public final class ReservaLugarDtos {

    private ReservaLugarDtos() {}

    public static ReservaLugarResponseDTO toDTO(ReservaLugar r, Position lugar) {
        return new ReservaLugarResponseDTO(
                r.getId().getStringValor(),
                r.getFuncionarioId().getStringValor(),
                texto(r.getPositionId()),
                lugar != null ? lugar.getNumeroLugar() : null,
                lugar != null ? texto(lugar.getUnidadeOrganicaId()) : null,
                texto(r.getGradeId()),
                texto(r.getFunctionId()),
                r.getNotes(),
                r.getEstado().name(),
                r.getReservadaEm(),
                r.getFechadaEm(),
                r.getMotivo(),
                texto(r.getAssignmentId()));
    }

    private static String texto(UUID v) {
        return v == null ? null : v.toString();
    }
}

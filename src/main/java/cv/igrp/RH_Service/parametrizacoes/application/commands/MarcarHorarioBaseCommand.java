package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@lombok.AllArgsConstructor
public class MarcarHorarioBaseCommand implements Command {
    private final String horarioId;
    /** Data de efeito (yyyy-MM-dd); nula = hoje. Uma data passada da 422. */
    private final java.time.LocalDate desde;

    public MarcarHorarioBaseCommand(String horarioId) {
        this(horarioId, null);
    }
}

package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;

import java.time.LocalDate;

/**
 * Desde quando um horário é o <b>base</b> da instituição. {@code desde} nulo quer dizer «desde
 * sempre»: é como fica registado o base que já existia antes de haver histórico.
 */
public record VigenciaHorarioBase(HorarioId horarioId, LocalDate desde) {

    /** Esta vigência já tinha começado em {@code data}. */
    public boolean comecouAte(LocalDate data) {
        return desde == null || !desde.isAfter(data);
    }
}

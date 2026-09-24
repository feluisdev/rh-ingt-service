package cv.igrp.RH_Service.parametrizacoes.application.port;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;

import java.time.LocalDate;

/**
 * <b>Um horário já vigorou?</b> Quem atribui horários (o colaborador, a unidade orgânica) responde
 * por si. O catálogo não conhece esses módulos — {@code parametrizacoes} não importa ninguém —, por
 * isso pergunta por esta porta, que eles implementam.
 */
public interface HorarioUtilizacaoPort {

    /** O horário esteve em vigor, por esta via, em algum dia antes de {@code data}. */
    boolean vigorouAntesDe(HorarioId horarioId, LocalDate data);
}

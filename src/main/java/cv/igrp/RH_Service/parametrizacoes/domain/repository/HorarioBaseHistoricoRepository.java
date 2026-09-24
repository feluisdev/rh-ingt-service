package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.models.VigenciaHorarioBase;

import java.time.LocalDate;
import java.util.List;

/** O histórico do horário base: uma linha por mudança, com a data de efeito. */
public interface HorarioBaseHistoricoRepository {
    List<VigenciaHorarioBase> findAll();
    /** Grava; uma vigência com o mesmo {@code desde} é substituída (reagendar no mesmo dia). */
    void registar(VigenciaHorarioBase vigencia);
    void apagarDesde(LocalDate desde);
}

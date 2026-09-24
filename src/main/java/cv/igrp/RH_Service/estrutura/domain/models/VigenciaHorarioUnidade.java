package cv.igrp.RH_Service.estrutura.domain.models;

import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;

import java.time.LocalDate;

/**
 * Desde quando uma unidade orgânica tem um horário. {@code horarioId} nulo = sem horário próprio (segue
 * a unidade-mãe); {@code desde} nulo = desde sempre (o horário que a unidade já tinha antes de haver
 * histórico).
 */
public record VigenciaHorarioUnidade(OrganizationalUnitId unidadeId, HorarioId horarioId, LocalDate desde) {

    public boolean comecouAte(LocalDate data) {
        return desde == null || !desde.isAfter(data);
    }
}

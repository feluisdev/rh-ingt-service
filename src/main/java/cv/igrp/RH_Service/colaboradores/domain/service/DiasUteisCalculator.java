package cv.igrp.RH_Service.colaboradores.domain.service;

import cv.igrp.RH_Service.parametrizacoes.domain.models.ContagemDias;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * Quantos dias tem uma ausência. Em {@link ContagemDias#DIAS_UTEIS}, os dias úteis do período; em
 * {@link ContagemDias#DIAS_SEGUIDOS} (art. 76.º do DL n.º 3/2010), os dias de calendário entre o
 * primeiro e o último dia útil — os fins-de-semana e feriados intercalados contam, os das pontas
 * não. Nos dois casos, um período sem nenhum dia útil é recusado: não há ausência de um dia em
 * que não havia dever de comparecer.
 */
public final class DiasUteisCalculator {

    public int calcular(LocalDate inicio, LocalDate fim, Set<LocalDate> feriados, ContagemDias contagem) {
        int uteis = calcular(inicio, fim, feriados);
        if (contagem != ContagemDias.DIAS_SEGUIDOS) return uteis;

        LocalDate primeiro = inicio;
        while (!eUtil(primeiro, feriados)) primeiro = primeiro.plusDays(1);
        LocalDate ultimo = fim;
        while (!eUtil(ultimo, feriados)) ultimo = ultimo.minusDays(1);
        return (int) ChronoUnit.DAYS.between(primeiro, ultimo) + 1;
    }

    private static boolean eUtil(LocalDate d, Set<LocalDate> feriados) {
        DayOfWeek dow = d.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY && !feriados.contains(d);
    }

    public int calcular(LocalDate inicio, LocalDate fim, Set<LocalDate> feriados) {
        if (inicio.isAfter(fim))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A data de início não pode ser posterior à data de fim.");

        int dias = 0;
        LocalDate current = inicio;
        while (!current.isAfter(fim)) {
            DayOfWeek dow = current.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY && !feriados.contains(current))
                dias++;
            current = current.plusDays(1);
        }

        if (dias == 0)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O período indicado não contém dias úteis.");

        return dias;
    }
}

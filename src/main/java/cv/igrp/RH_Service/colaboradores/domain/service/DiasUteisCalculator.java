package cv.igrp.RH_Service.colaboradores.domain.service;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public final class DiasUteisCalculator {

    public int calcular(LocalDate inicio, LocalDate fim, Set<LocalDate> feriadosNacionais) {
        if (inicio.isAfter(fim))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A data de início não pode ser posterior à data de fim.");

        int dias = 0;
        LocalDate current = inicio;
        while (!current.isAfter(fim)) {
            DayOfWeek dow = current.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY && !feriadosNacionais.contains(current))
                dias++;
            current = current.plusDays(1);
        }

        if (dias == 0)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O período indicado não contém dias úteis.");

        return dias;
    }
}

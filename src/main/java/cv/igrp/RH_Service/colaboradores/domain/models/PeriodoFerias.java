package cv.igrp.RH_Service.colaboradores.domain.models;

import java.time.LocalDate;

/**
 * Um período de férias: de {@code inicio} a {@code fim}, ambos incluídos.
 *
 * <p>{@code diasUteis} só existe na <b>marcação</b>, onde é contado com o calendário de feriados do
 * colaborador; na preferência fica nulo — é uma indicação, não um período que se vá gozar tal qual.
 */
public record PeriodoFerias(LocalDate inicio, LocalDate fim, Integer diasUteis) {

    public PeriodoFerias comDiasUteis(int dias) {
        return new PeriodoFerias(inicio, fim, dias);
    }

    public boolean sobrepoe(PeriodoFerias outro) {
        return !fim.isBefore(outro.inicio) && !outro.fim.isBefore(inicio);
    }

    public boolean dentroDe(LocalDate de, LocalDate ate) {
        return !inicio.isBefore(de) && !fim.isAfter(ate);
    }

    @Override
    public String toString() {
        return inicio + ".." + fim + (diasUteis != null ? " (" + diasUteis + ")" : "");
    }
}

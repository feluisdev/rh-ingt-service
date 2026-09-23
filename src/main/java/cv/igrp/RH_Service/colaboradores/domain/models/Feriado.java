package cv.igrp.RH_Service.colaboradores.domain.models;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Um feriado tal como a contagem de dias úteis o vê: uma data, e se se repete.
 *
 * <p>O feriado é gerido em {@code parametrizacoes} ({@code PublicHoliday}); aqui só se lê. Este
 * modelo substitui um agregado que nunca teve quem o gravasse e que guardava o «município» na
 * coluna da descrição (V55).
 *
 * <p><b>Recorrente</b>: no mesmo dia e mês todos os anos, a partir do ano de {@link #data()}.
 * Um 29 de Fevereiro recorrente é recusado na origem; se um aparecer, só cai nos bissextos.
 */
public record Feriado(LocalDate data, boolean recorrente) {

    /** As datas em que este feriado cai dentro de [{@code inicio}, {@code fim}]. */
    public Stream<LocalDate> ocorrenciasEntre(LocalDate inicio, LocalDate fim) {
        if (!recorrente)
            return dentro(data, inicio, fim) ? Stream.of(data) : Stream.empty();

        MonthDay diaDoAno = MonthDay.from(data);
        int primeiroAno = Math.max(inicio.getYear(), data.getYear());
        return IntStream.rangeClosed(primeiroAno, fim.getYear())
                .filter(diaDoAno::isValidYear)
                .mapToObj(diaDoAno::atYear)
                .filter(d -> dentro(d, inicio, fim));
    }

    private static boolean dentro(LocalDate d, LocalDate inicio, LocalDate fim) {
        return !d.isBefore(inicio) && !d.isAfter(fim);
    }
}

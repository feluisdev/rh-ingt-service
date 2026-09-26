package cv.igrp.RH_Service.colaboradores.domain.service;

import cv.igrp.RH_Service.colaboradores.domain.models.EspecieProcessoDisciplinar;

import java.time.LocalDate;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * <b>Quando se levanta auto</b> (Estatuto Disciplinar, arts. 80.º e 81.º n.º 3), contando só dias úteis de falta
 * injustificada:
 * <ul>
 *   <li><b>abandono de lugar</b>: 12 seguidos ou 15 interpolados no ano civil, ou 25 interpolados em 24 meses;</li>
 *   <li><b>falta de assiduidade</b>: 5 seguidos ou 8 interpolados no ano civil (alínea a); a alínea b), 8/12, é mais larga
 *   e fica coberta pela primeira).</li>
 * </ul>
 * O abandono consome a assiduidade: não se sugerem os dois.
 */
public final class RegrasAssiduidadeDisciplinar {

    public record Sinal(EspecieProcessoDisciplinar especie, String motivo, int seguidos, int noAno, int em24Meses) {}

    private RegrasAssiduidadeDisciplinar() {}

    public static Optional<Sinal> avaliar(NavigableSet<LocalDate> faltas, LocalDate dia, Predicate<LocalDate> util) {
        var ate = faltas.headSet(dia, true);
        var noAno = ate.tailSet(LocalDate.of(dia.getYear(), 1, 1), true);
        int total = noAno.size();
        int em24 = ate.tailSet(dia.minusMonths(24), false).size();
        int seguidos = maiorSequencia(noAno, util);
        if (seguidos >= 12 || total >= 15 || em24 >= 25) {
            String motivo = seguidos >= 12 ? seguidos + " dias úteis seguidos de falta injustificada (presume-se o abandono — art. 81.º n.º 1)"
                    : total >= 15 ? total + " dias úteis interpolados de falta injustificada no ano"
                    : em24 + " dias úteis interpolados de falta injustificada em 24 meses";
            return Optional.of(new Sinal(EspecieProcessoDisciplinar.ABANDONO_LUGAR, motivo, seguidos, total, em24));
        }
        if (seguidos >= 5 || total >= 8) {
            String motivo = seguidos >= 5 ? seguidos + " dias úteis seguidos de falta injustificada no ano"
                    : total + " dias úteis interpolados de falta injustificada no ano";
            return Optional.of(new Sinal(EspecieProcessoDisciplinar.FALTA_ASSIDUIDADE, motivo, seguidos, total, em24));
        }
        return Optional.empty();
    }

    /** A maior sequência de faltas em dias úteis consecutivos (o fim de semana e os feriados não a quebram). */
    static int maiorSequencia(NavigableSet<LocalDate> faltas, Predicate<LocalDate> util) {
        int maior = 0, corrente = 0;
        LocalDate anterior = null;
        for (LocalDate d : faltas) {
            corrente = anterior != null && d.equals(proximoUtil(anterior, util)) ? corrente + 1 : 1;
            maior = Math.max(maior, corrente);
            anterior = d;
        }
        return maior;
    }

    private static LocalDate proximoUtil(LocalDate d, Predicate<LocalDate> util) {
        LocalDate x = d.plusDays(1);
        for (int i = 0; i < 30 && !util.test(x); i++) x = x.plusDays(1);
        return x;
    }

    /** As datas úteis de um período. */
    public static List<LocalDate> uteis(LocalDate de, LocalDate ate, Predicate<LocalDate> util) {
        return de.datesUntil(ate.plusDays(1)).filter(util).toList();
    }
}

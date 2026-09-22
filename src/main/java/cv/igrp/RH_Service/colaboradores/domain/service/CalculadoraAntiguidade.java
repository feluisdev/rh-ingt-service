package cv.igrp.RH_Service.colaboradores.domain.service;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * <b>Tempo de serviço</b> — conta os dias entre duas datas descontando os períodos que a lei
 * manda não contar.
 *
 * <p>A antiguidade não se guarda: deriva-se do percurso. Guardá-la obrigaria a recalculá-la
 * sempre que uma data do passado fosse corrigida, e alguém acabaria por confiar num número
 * desactualizado.
 *
 * <p><b>Os períodos excluídos unem-se, não se somam.</b> É a decisão que faz este cálculo estar
 * certo. Uma licença sem vencimento de longa duração aparece <b>duas vezes</b> — uma pela
 * situação funcional em que põe o funcionário (inactividade fora do quadro, art. 121.º) e outra
 * pelo subtipo da própria licença (art. 47.º n.º 1) — e somá-las descontaria o dobro dos dias.
 * Sobrepostos, os intervalos fundem-se; disjuntos, ficam separados.
 *
 * <p>Conta-se em <b>dias de calendário</b>, com ambos os extremos incluídos: o que a lei desconta
 * é tempo, não dias de trabalho.
 */
public final class CalculadoraAntiguidade {

    /** Um período que não conta, e porquê. O motivo é para quem lê o resultado, não para a conta. */
    public record PeriodoExcluido(LocalDate inicio, LocalDate fim, String motivo) {

        /** Dias de calendário, extremos incluídos. */
        public long dias() {
            if (inicio == null || fim == null || fim.isBefore(inicio)) return 0;
            return ChronoUnit.DAYS.between(inicio, fim) + 1;
        }
    }

    /**
     * O resultado. Traz os períodos <b>efectivamente descontados</b> — já unidos e já recortados
     * ao intervalo de serviço —, e não os que foram dados à entrada: quem lê quer saber o que
     * pesou, não o que foi proposto.
     */
    public record Antiguidade(LocalDate dataInicio, LocalDate dataReferencia,
                              long diasTotais, long diasDescontados, long diasContados,
                              int anos, int meses, int dias,
                              List<PeriodoExcluido> periodosDescontados) {}

    private CalculadoraAntiguidade() {}

    /**
     * @param inicio         início da contagem — a admissão
     * @param referencia     data até à qual se conta (hoje, ou a data da cessação)
     * @param exclusoes      períodos que a lei manda não contar, em qualquer ordem e podendo
     *                       sobrepor-se
     */
    public static Antiguidade calcular(LocalDate inicio, LocalDate referencia,
                                       List<PeriodoExcluido> exclusoes) {
        if (inicio == null || referencia == null || referencia.isBefore(inicio))
            return new Antiguidade(inicio, referencia, 0, 0, 0, 0, 0, 0, List.of());

        long diasTotais = ChronoUnit.DAYS.between(inicio, referencia) + 1;

        List<PeriodoExcluido> descontados = unir(recortar(exclusoes, inicio, referencia));
        long diasDescontados = descontados.stream().mapToLong(PeriodoExcluido::dias).sum();
        long diasContados = Math.max(0, diasTotais - diasDescontados);

        Period emAnos = decompor(inicio, referencia, diasDescontados);

        return new Antiguidade(inicio, referencia, diasTotais, diasDescontados, diasContados,
                emAnos.getYears(), emAnos.getMonths(), emAnos.getDays(), descontados);
    }

    /**
     * Corta os períodos ao intervalo de serviço. Uma licença que começou antes da admissão, ou
     * que se prolonga para depois da data de referência, só desconta a parte que cai dentro —
     * caso contrário uma licença aberta descontaria tempo que ainda não passou.
     */
    private static List<PeriodoExcluido> recortar(List<PeriodoExcluido> exclusoes,
                                                  LocalDate inicio, LocalDate referencia) {
        List<PeriodoExcluido> recortados = new ArrayList<>();
        if (exclusoes == null) return recortados;

        for (PeriodoExcluido p : exclusoes) {
            if (p == null || p.inicio() == null) continue;
            // Período em aberto: conta até à data de referência, e não indefinidamente.
            LocalDate fim = p.fim() != null ? p.fim() : referencia;

            LocalDate de = p.inicio().isBefore(inicio) ? inicio : p.inicio();
            LocalDate ate = fim.isAfter(referencia) ? referencia : fim;
            if (ate.isBefore(de)) continue;

            recortados.add(new PeriodoExcluido(de, ate, p.motivo()));
        }
        return recortados;
    }

    /**
     * Funde os que se tocam ou sobrepõem. Dois períodos contíguos — um acaba na véspera de o
     * outro começar — também se fundem: entre eles não houve um dia de serviço.
     *
     * <p>Os motivos juntam-se separados por {@code " + "}, para que o resultado continue a dizer
     * <i>porquê</i> mesmo depois da fusão.
     */
    private static List<PeriodoExcluido> unir(List<PeriodoExcluido> periodos) {
        List<PeriodoExcluido> ordenados = new ArrayList<>(periodos);
        ordenados.sort(Comparator.comparing(PeriodoExcluido::inicio));

        List<PeriodoExcluido> unidos = new ArrayList<>();
        for (PeriodoExcluido p : ordenados) {
            if (unidos.isEmpty()) {
                unidos.add(p);
                continue;
            }
            PeriodoExcluido ultimo = unidos.get(unidos.size() - 1);

            if (p.inicio().isAfter(ultimo.fim().plusDays(1))) {
                unidos.add(p);
                continue;
            }

            LocalDate fim = p.fim().isAfter(ultimo.fim()) ? p.fim() : ultimo.fim();
            unidos.set(unidos.size() - 1,
                    new PeriodoExcluido(ultimo.inicio(), fim, juntarMotivos(ultimo.motivo(), p.motivo())));
        }
        return unidos;
    }

    private static String juntarMotivos(String a, String b) {
        if (a == null || a.isBlank()) return b;
        if (b == null || b.isBlank()) return a;
        if (a.equals(b) || a.contains(b)) return a;
        return a + " + " + b;
    }

    /**
     * Anos, meses e dias. Os dias descontados tiram-se do <b>fim</b> do intervalo, como se o
     * serviço tivesse acabado mais cedo — é assim que se lê «tem 8 anos de serviço»: o tempo
     * conta-se como se as interrupções nunca tivessem acontecido e o percurso fosse contínuo.
     */
    private static Period decompor(LocalDate inicio, LocalDate referencia, long diasDescontados) {
        LocalDate fimEfectivo = referencia.minusDays(diasDescontados);
        if (fimEfectivo.isBefore(inicio)) return Period.ZERO;
        // +1 dia porque o intervalo inclui os dois extremos, e Period.between exclui o fim.
        return Period.between(inicio, fimEfectivo.plusDays(1));
    }
}

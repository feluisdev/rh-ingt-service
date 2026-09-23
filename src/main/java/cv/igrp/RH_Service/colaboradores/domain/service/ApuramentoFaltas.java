package cv.igrp.RH_Service.colaboradores.domain.service;

import cv.igrp.RH_Service.colaboradores.domain.models.DiaAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoDiaApurado;
import cv.igrp.RH_Service.colaboradores.domain.models.MotivoFalta;
import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ControloHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.PeriodoAfericao;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>Apuramento de faltas por débito</b> — DL n.º 3/2010, art. 13.º; Lei n.º 20/X/2023, art. 170.º.
 *
 * <p>Puro: recebe cada dia do mês já classificado pelo serviço (feriado, ausência justificada,
 * licença, horário vigente, marcações) e devolve o tempo em falta. Não se guarda — até ao fecho do
 * mês (art. 75.º), que o congela.
 *
 * <ul>
 *   <li><b>Horário fixo</b>: em falta os minutos dos blocos que nenhum período de presença cobre
 *       (n.º 1: «a totalidade ou parte do período diário de presença obrigatória»).</li>
 *   <li><b>Horário flexível</b>: de dia a dia, as plataformas fixas não cobertas (n.º 1); no fim
 *       de cada período de aferição, o débito que falta, sem contar outra vez o que já contou (n.º 2).
 *       Uma semana partida pelo mês apura-se em cada mês pelos seus dias.</li>
 *   <li><b>Sem nenhuma marcação</b>: o dia inteiro, com o motivo SEM_REGISTO.</li>
 *   <li><b>Com anomalias</b>: POR_CORRIGIR — não se apura; inventar uma saída seria inventar uma falta.</li>
 * </ul>
 *
 * <p><b>Conversão (n.os 3 e 4)</b>: os dias sem registo são faltas de dia inteiro. O tempo parcial do
 * mês (incompletos, plataformas, débitos) <b>soma-se</b> — «é adicionada» — e converte-se pelo período
 * normal diário: cada período inteiro é uma falta; o resto, até meio período, meia; acima, uma.
 */
public final class ApuramentoFaltas {

    private static final BigDecimal MEIA = new BigDecimal("0.5");

    private ApuramentoFaltas() {}

    /**
     * Um dia do mês. {@code estadoPrevio} vem do serviço quando o dia não é de trabalho a apurar
     * (FUTURO, FERIADO, AUSENCIA_JUSTIFICADA...); nulo quer dizer «apurar pelo horário».
     */
    public record Dia(LocalDate data, EstadoDiaApurado estadoPrevio, Horario horario, DiaAssiduidade assiduidade,
                      boolean temMarcacoesValidas, List<DiaAssiduidade.Periodo> justificados) {
        /** Sem horas justificadas nesse dia. */
        public Dia(LocalDate data, EstadoDiaApurado estadoPrevio, Horario horario, DiaAssiduidade assiduidade,
                   boolean temMarcacoesValidas) {
            this(data, estadoPrevio, horario, assiduidade, temMarcacoesValidas, List.of());
        }
    }

    /**
     * {@code minutosJustificados}: as horas de pedidos em horas aprovados (V58) que não coincidem com
     * presença — contam como tempo cumprido.
     */
    public record DiaApurado(LocalDate data, EstadoDiaApurado estado, MotivoFalta motivo, int minutosEsperados,
                             int minutosTrabalhados, int minutosJustificados, int minutosEmFalta) {}

    public record Debito(String horarioNome, PeriodoAfericao periodo, LocalDate inicio, LocalDate fim,
                         int minutosEsperados, int minutosTrabalhados, int minutosJaEmFalta, int minutosDebito) {}

    public record Resultado(List<DiaApurado> dias, List<Debito> debitos, int diasSemRegisto, int minutosParciais,
                            int periodoNormalMinutos, BigDecimal faltasParciais, BigDecimal totalFaltas,
                            int diasPorCorrigir, int diasPorValidar) {}

    public static Resultado apurar(List<Dia> dias) {
        List<DiaApurado> apurados = new ArrayList<>();
        Map<String, List<Integer>> flexiveis = new LinkedHashMap<>();   // chave -> índices em apurados
        Map<String, Horario> horarioDaChave = new LinkedHashMap<>();

        for (Dia d : dias) {
            DiaApurado a = apurarDia(d);
            apurados.add(a);
            if (d.horario() != null && d.horario().getControlo() == ControloHorario.FLEXIVEL
                    && (a.estado() == EstadoDiaApurado.SEM_FALTA || a.estado() == EstadoDiaApurado.COM_FALTA)) {
                String chave = d.horario().getId().getStringValor() + "|" + periodo(d.data(), d.horario().getPeriodoAfericao());
                flexiveis.computeIfAbsent(chave, k -> new ArrayList<>()).add(apurados.size() - 1);
                horarioDaChave.putIfAbsent(chave, d.horario());
            }
        }

        List<Debito> debitos = new ArrayList<>();
        for (var e : flexiveis.entrySet()) {
            List<DiaApurado> doPeriodo = e.getValue().stream().map(apurados::get).toList();
            int esperados = doPeriodo.stream().mapToInt(DiaApurado::minutosEsperados).sum();
            int trabalhados = doPeriodo.stream().mapToInt(a -> a.minutosTrabalhados() + a.minutosJustificados()).sum();
            int jaEmFalta = doPeriodo.stream().mapToInt(DiaApurado::minutosEmFalta).sum();
            Horario h = horarioDaChave.get(e.getKey());
            debitos.add(new Debito(h.getNome(), h.getPeriodoAfericao(), doPeriodo.get(0).data(),
                    doPeriodo.get(doPeriodo.size() - 1).data(), esperados, trabalhados, jaEmFalta,
                    Math.max(0, esperados - trabalhados - jaEmFalta)));
        }

        int semRegisto = (int) apurados.stream().filter(a -> a.motivo() == MotivoFalta.SEM_REGISTO).count();
        int parciais = apurados.stream().filter(a -> a.motivo() == MotivoFalta.INCOMPLETO || a.motivo() == MotivoFalta.PLATAFORMA)
                .mapToInt(DiaApurado::minutosEmFalta).sum()
                + debitos.stream().mapToInt(Debito::minutosDebito).sum();
        int porCorrigir = (int) apurados.stream().filter(a -> a.estado() == EstadoDiaApurado.POR_CORRIGIR).count();
        int porValidar = (int) apurados.stream().filter(a -> a.estado() == EstadoDiaApurado.POR_VALIDAR).count();

        // O período normal diário de referência: a média do esperado nos dias de trabalho do mês.
        int[] esperadosTrabalho = apurados.stream()
                .filter(a -> a.minutosEsperados() > 0 && a.estado() != EstadoDiaApurado.POR_CORRIGIR)
                .mapToInt(DiaApurado::minutosEsperados).toArray();
        int referencia = esperadosTrabalho.length == 0 ? 0
                : (int) Math.round(java.util.Arrays.stream(esperadosTrabalho).average().orElse(0));

        BigDecimal faltasParciais = converter(parciais, referencia);
        return new Resultado(apurados, debitos, semRegisto, parciais, referencia, faltasParciais,
                faltasParciais.add(BigDecimal.valueOf(semRegisto)), porCorrigir, porValidar);
    }

    /** Art. 13.º n.º 4: cada período inteiro é uma falta; o resto, até meio período, meia; acima, uma. */
    static BigDecimal converter(int minutos, int periodoNormal) {
        if (minutos <= 0 || periodoNormal <= 0) return BigDecimal.ZERO;
        int inteiras = minutos / periodoNormal;
        int resto = minutos % periodoNormal;
        BigDecimal total = BigDecimal.valueOf(inteiras);
        if (resto == 0) return total;
        return total.add(resto * 2 <= periodoNormal ? MEIA : BigDecimal.ONE);
    }

    private static DiaApurado apurarDia(Dia d) {
        int trabalhados = d.assiduidade() != null ? d.assiduidade().minutosTrabalhados() : 0;
        if (d.estadoPrevio() != null)
            return new DiaApurado(d.data(), d.estadoPrevio(), null, 0, trabalhados, 0, 0);
        if (d.horario() == null)
            return new DiaApurado(d.data(), EstadoDiaApurado.SEM_HORARIO, null, 0, trabalhados, 0, 0);

        int esperados = d.horario().minutosNoDia(d.data().getDayOfWeek());
        if (esperados == 0)
            return new DiaApurado(d.data(), EstadoDiaApurado.DESCANSO, null, 0, trabalhados, 0, 0);
        if (d.assiduidade() != null && !d.assiduidade().anomalias().isEmpty())
            return new DiaApurado(d.data(), EstadoDiaApurado.POR_CORRIGIR, null, esperados, trabalhados, 0, 0);

        List<DiaAssiduidade.Periodo> justificados = d.justificados() != null ? d.justificados() : List.of();
        if (!d.temMarcacoesValidas() && justificados.isEmpty())
            return new DiaApurado(d.data(), EstadoDiaApurado.COM_FALTA, MotivoFalta.SEM_REGISTO, esperados, 0, 0, esperados);

        // Presença e horas justificadas juntam-se: uma hora picada que também foi justificada não
        // conta duas vezes.
        List<DiaAssiduidade.Periodo> presenca = d.assiduidade() != null ? d.assiduidade().periodos() : List.of();
        List<DiaAssiduidade.Periodo> cobertura = unir(presenca, justificados);
        int coberturaMinutos = cobertura.stream().mapToInt(DiaAssiduidade.Periodo::minutos).sum();
        int justificadosExtra = Math.max(0, coberturaMinutos - trabalhados);

        boolean flexivel = d.horario().getControlo() == ControloHorario.FLEXIVEL;
        List<BlocoHorario> obrigatorios = d.horario().getBlocos().stream()
                .filter(b -> b.dia() == d.data().getDayOfWeek())
                .filter(b -> !flexivel || b.obrigatorio())
                .toList();
        int emFalta = 0;
        for (BlocoHorario b : obrigatorios)
            emFalta += b.minutos() - coberto(b.inicio(), b.fim(), cobertura);
        if (emFalta <= 0)
            return new DiaApurado(d.data(), EstadoDiaApurado.SEM_FALTA, null, esperados, trabalhados, justificadosExtra, 0);
        return new DiaApurado(d.data(), EstadoDiaApurado.COM_FALTA, flexivel ? MotivoFalta.PLATAFORMA : MotivoFalta.INCOMPLETO,
                esperados, trabalhados, justificadosExtra, emFalta);
    }

    /** A união de dois conjuntos de períodos, sem sobreposições, por ordem. */
    static List<DiaAssiduidade.Periodo> unir(List<DiaAssiduidade.Periodo> a, List<DiaAssiduidade.Periodo> b) {
        List<DiaAssiduidade.Periodo> todos = new ArrayList<>(a);
        todos.addAll(b);
        todos.sort(java.util.Comparator.comparing(DiaAssiduidade.Periodo::entrada));
        List<DiaAssiduidade.Periodo> unidos = new ArrayList<>();
        for (DiaAssiduidade.Periodo p : todos) {
            if (!unidos.isEmpty() && !p.entrada().isAfter(unidos.get(unidos.size() - 1).saida())) {
                DiaAssiduidade.Periodo ultimo = unidos.remove(unidos.size() - 1);
                unidos.add(new DiaAssiduidade.Periodo(ultimo.entrada(),
                        p.saida().isAfter(ultimo.saida()) ? p.saida() : ultimo.saida()));
            } else {
                unidos.add(p);
            }
        }
        return unidos;
    }

    /** Minutos de [inicio, fim[ cobertos pelos períodos de presença (que não se sobrepõem entre si). */
    private static int coberto(LocalTime inicio, LocalTime fim, List<DiaAssiduidade.Periodo> periodos) {
        int total = 0;
        for (DiaAssiduidade.Periodo p : periodos) {
            LocalTime de = p.entrada().isAfter(inicio) ? p.entrada() : inicio;
            LocalTime ate = p.saida().isBefore(fim) ? p.saida() : fim;
            if (de.isBefore(ate)) total += (int) Duration.between(de, ate).toMinutes();
        }
        return total;
    }

    private static String periodo(LocalDate data, PeriodoAfericao periodo) {
        if (periodo == PeriodoAfericao.SEMANA)
            return "S" + data.get(WeekFields.ISO.weekBasedYear()) + "-" + data.get(WeekFields.ISO.weekOfWeekBasedYear());
        return "M" + data.getYear() + "-" + data.getMonthValue();
    }
}

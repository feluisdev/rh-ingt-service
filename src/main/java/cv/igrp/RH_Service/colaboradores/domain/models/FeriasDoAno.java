package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FeriasDoAnoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <b>As férias de um colaborador num ano</b> — a marcação do DL n.º 3/2010, arts. 5.º e 6.º.
 *
 * <p>Três coisas da mesma pessoa e do mesmo ano, e por isso um só agregado:
 * <ol>
 *   <li>a <b>preferência</b> que o trabalhador indica até 31 de Janeiro (art. 5.º n.º 4);</li>
 *   <li>a <b>marcação</b> que entra no mapa — por acordo (n.º 3) ou fixada pelo dirigente entre 1
 *       de Maio e 31 de Outubro (n.º 5);</li>
 *   <li>as <b>alterações</b> depois de o mapa ter sido dado a conhecer, cada uma com o motivo que o
 *       art. 6.º n.º 2 exige.</li>
 * </ol>
 *
 * <p><b>Marcar não é gozar.</b> O gozo continua a ser o pedido de férias, que desconta o saldo;
 * isto é o plano. Nada aqui mexe em saldos.
 */
@Getter
public class FeriasDoAno {

    private FeriasDoAnoId id;
    private FuncionarioId funcionarioId;
    private int ano;

    private List<PeriodoFerias> preferencia = new ArrayList<>();
    private LocalDate preferenciaIndicadaEm;
    private boolean preferenciaForaDePrazo;
    private String preferenciaObservacoes;

    private List<PeriodoFerias> marcacao = new ArrayList<>();
    private OrigemMarcacaoFerias origem;
    private String fundamentacao;
    private LocalDate marcadaEm;

    private List<AlteracaoMarcacaoFerias> alteracoes = new ArrayList<>();

    private FeriasDoAno() {}

    public static FeriasDoAno novo(FuncionarioId funcionarioId, int ano) {
        FeriasDoAno f = new FeriasDoAno();
        f.id = FeriasDoAnoId.gerarNovo();
        f.funcionarioId = funcionarioId;
        f.ano = ano;
        return f;
    }

    public static FeriasDoAno reconstituir(FeriasDoAnoId id, FuncionarioId funcionarioId, int ano,
                                          List<PeriodoFerias> preferencia, LocalDate preferenciaIndicadaEm,
                                          boolean preferenciaForaDePrazo, String preferenciaObservacoes,
                                          List<PeriodoFerias> marcacao, OrigemMarcacaoFerias origem,
                                          String fundamentacao, LocalDate marcadaEm,
                                          List<AlteracaoMarcacaoFerias> alteracoes) {
        FeriasDoAno f = new FeriasDoAno();
        f.id = id;
        f.funcionarioId = funcionarioId;
        f.ano = ano;
        f.preferencia = new ArrayList<>(preferencia);
        f.preferenciaIndicadaEm = preferenciaIndicadaEm;
        f.preferenciaForaDePrazo = preferenciaForaDePrazo;
        f.preferenciaObservacoes = preferenciaObservacoes;
        f.marcacao = new ArrayList<>(marcacao);
        f.origem = origem;
        f.fundamentacao = fundamentacao;
        f.marcadaEm = marcadaEm;
        f.alteracoes = new ArrayList<>(alteracoes);
        return f;
    }

    public boolean temPreferencia() { return !preferencia.isEmpty(); }
    public boolean temMarcacao() { return !marcacao.isEmpty(); }

    public int totalMarcado() {
        return marcacao.stream().mapToInt(p -> p.diasUteis() != null ? p.diasUteis() : 0).sum();
    }

    // ------------------------------------------------------------------ preferência

    /**
     * Art. 5.º n.º 4: «Até 31 de Janeiro de cada ano, devem os funcionários ou agentes indicar o
     * período do ano em que preferem gozar as férias.»
     *
     * <p><b>Fora do prazo não é recusada.</b> A lei diz que o trabalhador <i>deve</i> indicar até
     * lá, não que a indicação tardia é nula — e para o serviço, que ainda está a elaborar o mapa,
     * é informação útil. Fica marcada como tardia e a operação devolve o alerta.
     *
     * @return os alertas para o utilizador (vazio se não houver)
     */
    public List<String> indicarPreferencia(List<PeriodoFerias> periodos, String observacoes,
                                           LocalDate hoje, LocalDate prazo) {
        validarPeriodos(periodos, "A preferência");
        this.preferencia = ordenar(periodos);
        this.preferenciaObservacoes = observacoes;
        this.preferenciaIndicadaEm = hoje;
        this.preferenciaForaDePrazo = hoje.isAfter(prazo);

        List<String> alertas = new ArrayList<>();
        if (preferenciaForaDePrazo)
            alertas.add("Preferência indicada depois de " + prazo + " (art. 5.º n.º 4). Ficou registada "
                    + "como fora de prazo; sem acordo, o dirigente fixa as férias entre Maio e Outubro (n.º 5).");
        return alertas;
    }

    // ------------------------------------------------------------------ marcação

    /**
     * Marca (ou volta a marcar) as férias do ano — o que entra no mapa.
     *
     * @param periodos      os períodos, já com os dias úteis contados
     * @param mapaPublicado o mapa deste ano já foi dado a conhecer (art. 6.º n.º 1)
     * @param motivo        art. 6.º n.º 2: obrigatório para alterar uma marcação depois disso
     * @return os alertas para o utilizador
     */
    public List<String> marcar(List<PeriodoFerias> periodos, OrigemMarcacaoFerias origem,
                               String fundamentacao, RegrasMarcacaoFerias regras,
                               boolean mapaPublicado, MotivoAlteracaoMapaFerias motivo, LocalDate hoje) {
        validarPeriodos(periodos, "A marcação");
        String fund = fundamentacao == null || fundamentacao.isBlank() ? null : fundamentacao.trim();
        List<PeriodoFerias> ordenados = ordenar(periodos);

        validarArt5(ordenados, origem, fund, regras);

        // Art. 6.º n.º 2: depois de o mapa ter sido dado a conhecer, uma marcação que já lá estava
        // só muda por acordo ou por conveniência de serviço fundamentada. A primeira marcação de
        // quem não estava no mapa (um admitido depois, por exemplo) não é uma alteração.
        boolean eAlteracao = mapaPublicado && temMarcacao();
        if (eAlteracao) {
            if (motivo == null)
                throw erro("O mapa de " + ano + " já foi dado a conhecer: alterar esta marcação exige o motivo "
                        + "do art. 6.º n.º 2 — ACORDO ou CONVENIENCIA_SERVICO.");
            if (motivo == MotivoAlteracaoMapaFerias.CONVENIENCIA_SERVICO && fund == null)
                throw erro("A alteração por conveniência de serviço tem de ser «devidamente fundamentada» "
                        + "(art. 6.º n.º 2): falta a fundamentação.");
            alteracoes.add(new AlteracaoMarcacaoFerias(motivo, fund, texto(marcacao), texto(ordenados), hoje));
        }

        this.marcacao = ordenados;
        this.origem = origem;
        this.fundamentacao = fund;
        this.marcadaEm = hoje;

        List<String> alertas = new ArrayList<>();
        if (regras.direitoTotal() == null)
            alertas.add("Não há tipo de férias no catálogo: o total marcado não foi comparado com o direito.");
        else if (totalMarcado() < regras.direitoTotal())
            alertas.add("Ficam " + (regras.direitoTotal() - totalMarcado()) + " dia(s) de férias por marcar em " + ano + ".");
        return alertas;
    }

    private void validarArt5(List<PeriodoFerias> periodos, OrigemMarcacaoFerias origem, String fundamentacao,
                             RegrasMarcacaoFerias regras) {
        int total = periodos.stream().mapToInt(PeriodoFerias::diasUteis).sum();

        for (PeriodoFerias p : periodos)
            if (p.diasUteis() == null || p.diasUteis() <= 0)
                throw erro("O período " + p.inicio() + " a " + p.fim() + " não tem dias úteis.");

        if (regras.direitoTotal() != null && total > regras.direitoTotal())
            throw erro("A marcação soma " + total + " dias úteis e o direito de " + ano + " é de "
                    + regras.direitoTotal() + ".");

        // Art. 5.º n.º 1, primeira parte: não se gozam seguidamente mais dias úteis do que o
        // direito anual do art. 2.º n.º 3.
        for (PeriodoFerias p : periodos)
            if (p.diasUteis() > regras.maximoSeguidos())
                throw erro("Não se podem gozar seguidamente mais de " + regras.maximoSeguidos()
                        + " dias úteis (art. 5.º n.º 1); o período de " + p.inicio() + " tem " + p.diasUteis() + ".");

        boolean interpolado = periodos.size() > 1;

        // Art. 5.º n.º 1, segunda parte: em gozo interpolado, um dos períodos não pode ser
        // inferior a 11 dias — salvo o ano de ingresso (art. 3.º), em que o direito é pequeno de
        // mais para isso. Só se exige quando o total o permite: com 8 dias de direito ninguém
        // consegue um período de 11.
        if (interpolado && !regras.anoDeIngresso() && total >= regras.minimoInterpolado()
                && periodos.stream().noneMatch(p -> p.diasUteis() >= regras.minimoInterpolado()))
            throw erro("Em gozo interpolado, um dos períodos tem de ter pelo menos "
                    + regras.minimoInterpolado() + " dias úteis (art. 5.º n.º 1).");

        if (origem == OrigemMarcacaoFerias.FIXADA) {
            // Art. 5.º n.º 5: na falta de acordo, o dirigente fixa entre 1 de Maio e 31 de Outubro.
            for (PeriodoFerias p : periodos)
                if (!p.dentroDe(regras.janelaInicio(), regras.janelaFim()))
                    throw erro("Sem acordo, as férias são fixadas entre " + regras.janelaInicio() + " e "
                            + regras.janelaFim() + " (art. 5.º n.º 5); o período de " + p.inicio()
                            + " a " + p.fim() + " fica fora.");

            // Art. 5.º n.º 2: o gozo interpolado não pode ser imposto, salvo conveniência de serviço
            // devidamente fundamentada. Por acordo, o trabalhador aceitou-o — não há imposição.
            if (interpolado && fundamentacao == null)
                throw erro("Fixar as férias em mais de um período impõe o gozo interpolado, e isso exige "
                        + "conveniência de serviço devidamente fundamentada (art. 5.º n.º 2).");
        }
    }

    private void validarPeriodos(List<PeriodoFerias> periodos, String oQue) {
        if (periodos == null || periodos.isEmpty())
            throw erro(oQue + " tem de ter pelo menos um período.");
        LocalDate de = LocalDate.of(ano, 1, 1);
        LocalDate ate = LocalDate.of(ano, 12, 31);
        for (PeriodoFerias p : periodos) {
            if (p.inicio() == null || p.fim() == null)
                throw erro("Cada período tem de ter data de início e de fim.");
            if (p.inicio().isAfter(p.fim()))
                throw erro("O período " + p.inicio() + " a " + p.fim() + " acaba antes de começar.");
            if (!p.dentroDe(de, ate))
                throw erro("O período " + p.inicio() + " a " + p.fim() + " não é de " + ano + ".");
        }
        List<PeriodoFerias> ord = ordenar(periodos);
        for (int i = 1; i < ord.size(); i++)
            if (ord.get(i).sobrepoe(ord.get(i - 1)))
                throw erro("Os períodos " + ord.get(i - 1) + " e " + ord.get(i) + " sobrepõem-se.");
    }

    private static List<PeriodoFerias> ordenar(List<PeriodoFerias> periodos) {
        return periodos.stream().sorted(Comparator.comparing(PeriodoFerias::inicio)).collect(Collectors.toList());
    }

    private static String texto(List<PeriodoFerias> periodos) {
        return periodos.stream().map(PeriodoFerias::toString).collect(Collectors.joining("; "));
    }

    private static IgrpResponseStatusException erro(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}

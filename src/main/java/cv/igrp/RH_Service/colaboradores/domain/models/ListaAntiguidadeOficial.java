package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ListaAntiguidadeOficialId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * <b>A lista de antiguidade oficial</b> de um serviço num ano (DL n.º 3/2010, arts. 69.º–74.º; BR-LAN-06..15):
 * a lista gerada (BR-LAN-01..05) <b>congela-se</b> na aprovação pelo dirigente — as linhas guardam-se, é um
 * documento — e segue o ciclo da lei: afixação (abre o prazo de reclamação de 30 dias consecutivos, 60 para
 * quem presta serviço no estrangeiro), decisão das reclamações, lista definitiva e publicação no Boletim
 * Oficial até 30 de Abril.
 *
 * <p>APROVADA → AFIXADA → DEFINITIVA → PUBLICADA; ANULADA (antes de publicada). Enquanto não é definitiva,
 * {@link #recalcular} volta a gerar as linhas (depois de corrigidos os dados de uma reclamação deferida).
 */
@Getter
public class ListaAntiguidadeOficial {

    public enum Estado { APROVADA, AFIXADA, DEFINITIVA, PUBLICADA, ANULADA }

    /** DL n.º 3/2010, art. 72.º n.º 1: 30 dias consecutivos a contar da afixação. */
    public static final int PRAZO_RECLAMACAO = 30;
    /** Art. 74.º: 60 dias para quem presta serviço no estrangeiro. */
    public static final int PRAZO_RECLAMACAO_ESTRANGEIRO = 60;
    /** Art. 71.º n.º 2: publicada até 30 de Abril. */
    public static final MonthDay LIMITE_PUBLICACAO = MonthDay.of(4, 30);

    /** Uma linha congelada: tudo o que o art. 69.º n.º 2 manda mostrar, como estava na aprovação. */
    public record Linha(int grupo, String carreira, String categoria, boolean foraDeGrelha, int posicao,
                        FuncionarioId funcionarioId, String numeroFuncionario, String nome, UUID unidadeId, String escalao,
                        LocalDate inicioNoCargo, long diasDescontados, long diasNoCargo, int anosNoCargo, int mesesNoCargo,
                        int diasNoCargoResto, long diasTotais, int anosTotal, int mesesTotal, int diasTotalResto) {}

    private ListaAntiguidadeOficialId id;
    private int ano;
    private LocalDate referencia;
    private UUID unidadeId;
    private boolean incluirSubunidades;
    private Estado estado;
    private String aprovadaPor;
    private LocalDate dataAprovacao;
    private LocalDate dataAfixacao;
    private String localAfixacao;
    private LocalDate dataDefinitiva;
    private String publicacaoSerie;
    private String publicacaoNumero;
    private LocalDate publicacaoData;
    private String motivoAnulacao;
    private int versao;
    private List<Linha> linhas = new ArrayList<>();

    private ListaAntiguidadeOficial() {}

    /** Aprovar é congelar: a lista gerada hoje fica como está. */
    public static ListaAntiguidadeOficial aprovar(int ano, UUID unidadeId, boolean incluirSubunidades, String aprovadaPor,
                                                  LocalDate dataAprovacao, List<Linha> linhas) {
        Objects.requireNonNull(unidadeId);
        if (aprovadaPor == null || aprovadaPor.isBlank())
            throw invalido("Indique quem aprova a lista: o dirigente do serviço (art. 71.º n.º 1).");
        var l = new ListaAntiguidadeOficial();
        l.id = ListaAntiguidadeOficialId.gerarNovo();
        l.ano = ano;
        l.referencia = LocalDate.of(ano - 1, 12, 31);
        l.unidadeId = unidadeId;
        l.incluirSubunidades = incluirSubunidades;
        l.estado = Estado.APROVADA;
        l.aprovadaPor = aprovadaPor.trim();
        l.dataAprovacao = Objects.requireNonNull(dataAprovacao);
        l.linhas = new ArrayList<>(linhas);
        l.versao = 1;
        return l;
    }

    public static ListaAntiguidadeOficial reconstruir(ListaAntiguidadeOficialId id, int ano, LocalDate referencia, UUID unidadeId,
                                                     boolean incluirSubunidades, Estado estado, String aprovadaPor,
                                                     LocalDate dataAprovacao, LocalDate dataAfixacao, String localAfixacao,
                                                     LocalDate dataDefinitiva, String publicacaoSerie, String publicacaoNumero,
                                                     LocalDate publicacaoData, String motivoAnulacao, int versao, List<Linha> linhas) {
        var l = new ListaAntiguidadeOficial();
        l.id = id;
        l.ano = ano;
        l.referencia = referencia;
        l.unidadeId = unidadeId;
        l.incluirSubunidades = incluirSubunidades;
        l.estado = estado;
        l.aprovadaPor = aprovadaPor;
        l.dataAprovacao = dataAprovacao;
        l.dataAfixacao = dataAfixacao;
        l.localAfixacao = localAfixacao;
        l.dataDefinitiva = dataDefinitiva;
        l.publicacaoSerie = publicacaoSerie;
        l.publicacaoNumero = publicacaoNumero;
        l.publicacaoData = publicacaoData;
        l.motivoAnulacao = motivoAnulacao;
        l.versao = versao;
        l.linhas = new ArrayList<>(linhas);
        return l;
    }

    /** Art. 71.º n.º 1: afixada em local previamente anunciado; abre o prazo de reclamação. */
    public void afixar(LocalDate data, String local) {
        exigir(Estado.APROVADA, "afixar");
        if (data == null) throw invalido("Indique a data da afixação.");
        if (data.isBefore(dataAprovacao)) throw invalido("A lista não se afixa antes de ser aprovada (" + Datas.pt(dataAprovacao) + ").");
        if (local == null || local.isBlank()) throw invalido("Indique o local da afixação, anunciado aos funcionários.");
        this.dataAfixacao = data;
        this.localAfixacao = local.trim();
        this.estado = Estado.AFIXADA;
    }

    /** O último dia para reclamar (30 dias consecutivos depois da afixação; 60 no estrangeiro). */
    public LocalDate fimPrazoReclamacao(boolean noEstrangeiro) {
        if (dataAfixacao == null) return null;
        return dataAfixacao.plusDays(noEstrangeiro ? PRAZO_RECLAMACAO_ESTRANGEIRO : PRAZO_RECLAMACAO);
    }

    public boolean aceitaReclamacao(LocalDate em, boolean noEstrangeiro) {
        return estado == Estado.AFIXADA && !em.isBefore(dataAfixacao) && !em.isAfter(fimPrazoReclamacao(noEstrangeiro));
    }

    /** Volta a gerar as linhas (os dados de uma reclamação deferida foram corrigidos). */
    public void recalcular(List<Linha> novas) {
        if (estado != Estado.APROVADA && estado != Estado.AFIXADA)
            throw IgrpResponseStatusException.conflict("Uma lista definitiva ou publicada já não se recalcula.");
        this.linhas = new ArrayList<>(novas);
        this.versao++;
    }

    /**
     * Definitiva depois das reclamações: com o prazo geral esgotado e nenhuma reclamação por decidir (a
     * verificação das reclamações é do serviço, que as conhece).
     */
    public void tornarDefinitiva(LocalDate hoje, boolean haReclamacoesPorDecidir) {
        exigir(Estado.AFIXADA, "tornar definitiva");
        if (!hoje.isAfter(fimPrazoReclamacao(false)))
            throw invalido("O prazo de reclamação só termina a " + Datas.pt(fimPrazoReclamacao(false)) + ".");
        if (haReclamacoesPorDecidir)
            throw IgrpResponseStatusException.conflict("Há reclamações por decidir: decida-as antes de tornar a lista definitiva.");
        this.dataDefinitiva = hoje;
        this.estado = Estado.DEFINITIVA;
    }

    /** Art. 71.º n.º 2: publicada no Boletim Oficial. Depois de 30 de Abril publica-se na mesma — a mensagem avisa. */
    public boolean publicar(String serie, String numero, LocalDate data) {
        exigir(Estado.DEFINITIVA, "publicar");
        if (numero == null || numero.isBlank() || data == null)
            throw invalido("Indique o número e a data do Boletim Oficial em que a lista foi publicada.");
        this.publicacaoSerie = serie == null || serie.isBlank() ? null : serie.trim();
        this.publicacaoNumero = numero.trim();
        this.publicacaoData = data;
        this.estado = Estado.PUBLICADA;
        return foraDoPrazoDePublicacao();
    }

    public boolean foraDoPrazoDePublicacao() {
        return publicacaoData != null && publicacaoData.isAfter(LIMITE_PUBLICACAO.atYear(ano));
    }

    public void anular(String motivo) {
        if (estado == Estado.PUBLICADA || estado == Estado.ANULADA)
            throw IgrpResponseStatusException.conflict("Uma lista publicada (ou já anulada) não se anula.");
        if (motivo == null || motivo.isBlank()) throw invalido("Anular a lista exige o motivo.");
        this.motivoAnulacao = motivo.trim();
        this.estado = Estado.ANULADA;
    }

    public boolean contem(FuncionarioId funcionarioId) {
        return linhas.stream().anyMatch(l -> l.funcionarioId().equals(funcionarioId));
    }

    private void exigir(Estado esperado, String accao) {
        if (estado != esperado)
            throw IgrpResponseStatusException.conflict("Não é possível " + accao + " a lista no estado em que está.");
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}

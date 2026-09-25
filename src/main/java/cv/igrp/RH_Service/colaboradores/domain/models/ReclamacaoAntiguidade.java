package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ListaAntiguidadeOficialId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ReclamacaoAntiguidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * <b>Reclamação da lista de antiguidade</b> (DL n.º 3/2010, arts. 72.º–74.º; BR-LAN-10..14): apresentada no
 * prazo, com um dos fundamentos da lei, decidida pelo dirigente (a decisão notifica-se em 30 dias), e com
 * recurso para o membro do Governo em 20 dias (60 no estrangeiro) a contar da notificação.
 */
@Getter
public class ReclamacaoAntiguidade {

    /** Art. 72.º n.º 2. */
    public enum Fundamento { OMISSAO, GRADUACAO, SITUACAO, CONTAGEM }

    public enum Estado { APRESENTADA, DEFERIDA, INDEFERIDA }

    public enum ResultadoRecurso { PROVIDO, NAO_PROVIDO }

    /** Art. 72.º n.º 5: a decisão notifica-se em 30 dias. */
    public static final int PRAZO_DECISAO = 30;
    /** Art. 73.º n.º 1: recurso em 20 dias... */
    public static final int PRAZO_RECURSO = 20;
    /** ... 60 no estrangeiro (art. 74.º). */
    public static final int PRAZO_RECURSO_ESTRANGEIRO = 60;

    private ReclamacaoAntiguidadeId id;
    private ListaAntiguidadeOficialId listaId;
    private FuncionarioId funcionarioId;
    private Fundamento fundamento;
    private String texto;
    private boolean noEstrangeiro;
    private boolean peloProprio;
    private LocalDate dataApresentacao;
    private Estado estado;
    private String decisao;
    private LocalDate dataDecisao;
    private LocalDate recursoEm;
    private String recursoTexto;
    private ResultadoRecurso recursoResultado;
    private String recursoDecisao;
    private LocalDate recursoDecididoEm;

    private ReclamacaoAntiguidade() {}

    public static ReclamacaoAntiguidade apresentar(ListaAntiguidadeOficial lista, FuncionarioId funcionarioId, Fundamento fundamento,
                                                   String texto, boolean noEstrangeiro, boolean peloProprio, LocalDate data) {
        Objects.requireNonNull(lista);
        Objects.requireNonNull(funcionarioId);
        if (fundamento == null)
            throw invalido("Indique o fundamento da reclamação: omissão, graduação, situação na lista ou contagem do tempo.");
        if (texto == null || texto.isBlank()) throw invalido("Descreva a reclamação.");
        if (!lista.aceitaReclamacao(data, noEstrangeiro))
            throw invalido(lista.getEstado() != ListaAntiguidadeOficial.Estado.AFIXADA
                    ? "A lista não está afixada: não aceita reclamações."
                    : "O prazo de reclamação terminou a " + Datas.pt(lista.fimPrazoReclamacao(noEstrangeiro)) + ".");
        if (fundamento != Fundamento.OMISSAO && !lista.contem(funcionarioId))
            throw invalido("Este colaborador não consta da lista: a única reclamação possível é a de omissão.");
        var r = new ReclamacaoAntiguidade();
        r.id = ReclamacaoAntiguidadeId.gerarNovo();
        r.listaId = lista.getId();
        r.funcionarioId = funcionarioId;
        r.fundamento = fundamento;
        r.texto = texto.trim();
        r.noEstrangeiro = noEstrangeiro;
        r.peloProprio = peloProprio;
        r.dataApresentacao = data;
        r.estado = Estado.APRESENTADA;
        return r;
    }

    public static ReclamacaoAntiguidade reconstruir(ReclamacaoAntiguidadeId id, ListaAntiguidadeOficialId listaId,
                                                    FuncionarioId funcionarioId, Fundamento fundamento, String texto,
                                                    boolean noEstrangeiro, boolean peloProprio, LocalDate dataApresentacao,
                                                    Estado estado, String decisao, LocalDate dataDecisao, LocalDate recursoEm,
                                                    String recursoTexto, ResultadoRecurso recursoResultado,
                                                    String recursoDecisao, LocalDate recursoDecididoEm) {
        var r = new ReclamacaoAntiguidade();
        r.id = id;
        r.listaId = listaId;
        r.funcionarioId = funcionarioId;
        r.fundamento = fundamento;
        r.texto = texto;
        r.noEstrangeiro = noEstrangeiro;
        r.peloProprio = peloProprio;
        r.dataApresentacao = dataApresentacao;
        r.estado = estado;
        r.decisao = decisao;
        r.dataDecisao = dataDecisao;
        r.recursoEm = recursoEm;
        r.recursoTexto = recursoTexto;
        r.recursoResultado = recursoResultado;
        r.recursoDecisao = recursoDecisao;
        r.recursoDecididoEm = recursoDecididoEm;
        return r;
    }

    /** Art. 72.º n.º 4: decide o dirigente. Devolve se foi decidida fora dos 30 dias (aviso, não bloqueio). */
    public boolean decidir(boolean deferida, String decisao, LocalDate data) {
        if (estado != Estado.APRESENTADA) throw IgrpResponseStatusException.conflict("Esta reclamação já foi decidida.");
        if (decisao == null || decisao.isBlank()) throw invalido("Fundamente a decisão da reclamação.");
        this.estado = deferida ? Estado.DEFERIDA : Estado.INDEFERIDA;
        this.decisao = decisao.trim();
        this.dataDecisao = Objects.requireNonNull(data);
        return data.isAfter(dataApresentacao.plusDays(PRAZO_DECISAO));
    }

    /** Art. 73.º: recurso da decisão, no prazo. */
    public void recorrer(String texto, LocalDate data) {
        if (estado == Estado.APRESENTADA) throw IgrpResponseStatusException.conflict("A reclamação ainda não foi decidida.");
        if (recursoEm != null) throw IgrpResponseStatusException.conflict("Já foi interposto recurso desta decisão.");
        if (texto == null || texto.isBlank()) throw invalido("Descreva o recurso.");
        LocalDate fim = dataDecisao.plusDays(noEstrangeiro ? PRAZO_RECURSO_ESTRANGEIRO : PRAZO_RECURSO);
        if (data.isAfter(fim)) throw invalido("O prazo de recurso terminou a " + Datas.pt(fim) + ".");
        this.recursoEm = data;
        this.recursoTexto = texto.trim();
    }

    public void decidirRecurso(boolean provido, String decisao, LocalDate data) {
        if (recursoEm == null) throw IgrpResponseStatusException.conflict("Não há recurso desta reclamação.");
        if (recursoResultado != null) throw IgrpResponseStatusException.conflict("O recurso já foi decidido.");
        if (decisao == null || decisao.isBlank()) throw invalido("Fundamente a decisão do recurso.");
        this.recursoResultado = provido ? ResultadoRecurso.PROVIDO : ResultadoRecurso.NAO_PROVIDO;
        this.recursoDecisao = decisao.trim();
        this.recursoDecididoEm = data;
    }

    public boolean porDecidir() {
        return estado == Estado.APRESENTADA;
    }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}

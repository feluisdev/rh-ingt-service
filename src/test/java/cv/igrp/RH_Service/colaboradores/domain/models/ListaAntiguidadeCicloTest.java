package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** DL n.o 3/2010, arts. 71.o-74.o: afixacao, prazos de reclamacao e recurso, definitiva, publicacao ate 30/4. */
class ListaAntiguidadeCicloTest {

    private final FuncionarioId maria = FuncionarioId.gerarNovo();
    private final FuncionarioId fora = FuncionarioId.gerarNovo();

    private static int status(Runnable r) {
        return assertThrows(IgrpResponseStatusException.class, r::run).getStatusCode().value();
    }

    private ListaAntiguidadeOficial lista() {
        var linha = new ListaAntiguidadeOficial.Linha(1, "Regime Geral", "Tecnico Superior", false, 1, maria, "0000002", "Maria",
                UUID.randomUUID(), "Escalao 1", LocalDate.of(2015, 1, 1), 0, 3652, 10, 0, 2, 5844, 16, 0, 4);
        return ListaAntiguidadeOficial.aprovar(2026, UUID.randomUUID(), true, "Director", LocalDate.of(2026, 2, 1), List.of(linha));
    }

    @Test
    void aprovadaCongelaAsLinhasDeReferenciaA31DeDezembro() {
        var l = lista();
        assertEquals(LocalDate.of(2025, 12, 31), l.getReferencia());
        assertEquals(1, l.getLinhas().size());
        assertEquals(422, status(() -> ListaAntiguidadeOficial.aprovar(2026, UUID.randomUUID(), true, " ", LocalDate.now(), List.of())));
    }

    @Test
    void prazosDeReclamacao30Ou60DiasDepoisDaAfixacao() {
        var l = lista();
        assertEquals(422, status(() -> l.afixar(LocalDate.of(2026, 1, 1), "Atrio")));   // antes da aprovacao
        l.afixar(LocalDate.of(2026, 2, 10), "Atrio do edificio");
        assertEquals(LocalDate.of(2026, 3, 12), l.fimPrazoReclamacao(false));
        assertEquals(LocalDate.of(2026, 4, 11), l.fimPrazoReclamacao(true));
        assertTrue(l.aceitaReclamacao(LocalDate.of(2026, 3, 12), false));
        assertFalse(l.aceitaReclamacao(LocalDate.of(2026, 3, 13), false));
        assertTrue(l.aceitaReclamacao(LocalDate.of(2026, 3, 13), true));
    }

    @Test
    void reclamacaoForaDoPrazoOuDeQuemNaoConstaSoPorOmissao() {
        var l = lista();
        l.afixar(LocalDate.of(2026, 2, 10), "Atrio");
        assertEquals(422, status(() -> ReclamacaoAntiguidade.apresentar(l, maria, ReclamacaoAntiguidade.Fundamento.CONTAGEM, "x",
                false, true, LocalDate.of(2026, 3, 20))));
        assertEquals(422, status(() -> ReclamacaoAntiguidade.apresentar(l, fora, ReclamacaoAntiguidade.Fundamento.GRADUACAO, "x",
                false, true, LocalDate.of(2026, 2, 20))));
        var omissao = ReclamacaoAntiguidade.apresentar(l, fora, ReclamacaoAntiguidade.Fundamento.OMISSAO, "Nao consto", false, true,
                LocalDate.of(2026, 2, 20));
        assertTrue(omissao.porDecidir());
    }

    @Test
    void decisaoForaDos30DiasAvisaERecursoEm20() {
        var l = lista();
        l.afixar(LocalDate.of(2026, 2, 10), "Atrio");
        var r = ReclamacaoAntiguidade.apresentar(l, maria, ReclamacaoAntiguidade.Fundamento.CONTAGEM, "Faltam 30 dias", false, true,
                LocalDate.of(2026, 2, 15));
        assertEquals(409, status(() -> r.recorrer("x", LocalDate.of(2026, 2, 20))));   // por decidir
        assertTrue(r.decidir(false, "Contagem confirmada", LocalDate.of(2026, 3, 20)));    // 33 dias: fora do prazo
        assertEquals(422, status(() -> r.recorrer("Discordo", LocalDate.of(2026, 4, 10))));
        r.recorrer("Discordo", LocalDate.of(2026, 4, 9));
        r.decidirRecurso(true, "Provido", LocalDate.of(2026, 4, 20));
        assertEquals(ReclamacaoAntiguidade.ResultadoRecurso.PROVIDO, r.getRecursoResultado());
    }

    @Test
    void definitivaSoDepoisDoPrazoESemReclamacoesPorDecidirEPublicacaoAte30DeAbril() {
        var l = lista();
        l.afixar(LocalDate.of(2026, 2, 10), "Atrio");
        assertEquals(422, status(() -> l.tornarDefinitiva(LocalDate.of(2026, 3, 12), false)));
        assertEquals(409, status(() -> l.tornarDefinitiva(LocalDate.of(2026, 3, 13), true)));
        l.tornarDefinitiva(LocalDate.of(2026, 3, 13), false);
        assertEquals(409, status(() -> l.recalcular(List.of())));
        assertTrue(l.publicar("II", "57", LocalDate.of(2026, 5, 2)));   // fora do prazo: avisa
        assertEquals(ListaAntiguidadeOficial.Estado.PUBLICADA, l.getEstado());
        assertEquals(409, status(() -> l.anular("x")));
    }
}

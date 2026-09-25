package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoEmitidoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.services.documentos.GeradorPdf;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

/** Lei n.o 20/X/2023, art. 25.o: entrega atestada, validade pela funcao e categoria, devolucao e anulacao. */
class CartaoProfissionalTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 25);

    private CartaoProfissional cartao() {
        return CartaoProfissional.emitir(FuncionarioId.gerarNovo(), DocumentoEmitidoId.gerarNovo(), "CIP-2026-000001", HOJE,
                "cat-1", "Tecnico Superior", "fun-1", "Gestor de RH", "Gestor");
    }

    @Test
    void valeEnquantoNaMesmaFuncaoECategoria() {
        var c = cartao();
        assertNull(c.motivoInvalidade(true, "cat-1", "fun-1"));
        assertTrue(c.motivoInvalidade(true, "cat-2", "fun-1").contains("categoria"));
        assertTrue(c.motivoInvalidade(true, "cat-1", "fun-2").contains("função"));
        assertTrue(c.motivoInvalidade(false, "cat-1", "fun-1").contains("já não está ao serviço"));
    }

    @Test
    void entregaDevolucaoEAnulacao() {
        var c = cartao();
        assertEquals(422, assertThrows(IgrpResponseStatusException.class, () -> c.entregar(HOJE.minusDays(1))).getStatusCode().value());
        c.entregar(HOJE.plusDays(2));
        assertEquals(409, assertThrows(IgrpResponseStatusException.class, () -> c.entregar(HOJE.plusDays(3))).getStatusCode().value());
        c.devolver(HOJE.plusDays(30));
        assertEquals(CartaoProfissional.Estado.DEVOLVIDO, c.getEstado());
        assertEquals(409, assertThrows(IgrpResponseStatusException.class, () -> c.anular("perda")).getStatusCode().value());
        assertTrue(c.motivoInvalidade(true, "cat-1", "fun-1").contains("devolvido"));
    }

    @Test
    void oModeloDoCartaoDaUmPdf() {
        var g = new GeradorPdf();
        var v = new HashMap<String, Object>();
        v.put("titulo", "Cartão");
        v.put("instituicao", "Instituição de teste");
        v.put("nome", "Maria Lopes");
        v.put("numeroFuncionario", "0000002");
        v.put("categoria", "Técnico Superior");
        v.put("funcao", "Gestora de RH");
        v.put("unidade", "Serviço de RH");
        v.put("numero", "CIP-2026-000001");
        v.put("codigo", "ABCDEFGH23");
        v.put("dataEmissao", "25/09/2026");
        v.put("urlVerificacao", "/api/v1/rh/verificacao/documentos/ABCDEFGH23");
        byte[] pdf = g.pdf(g.preencher("cartao", v));
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }
}

package cv.igrp.RH_Service.shared.application.services.documentos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** O modelo da declaracao (Thymeleaf) preenche-se e da um PDF: prova que o XHTML e o CSS servem o openhtmltopdf. */
class GeradorPdfTest {

    private final GeradorPdf gerador = new GeradorPdf();

    private Map<String, Object> valores() {
        Map<String, Object> v = new HashMap<>();
        v.put("titulo", "Declaração de vínculo");
        v.put("instituicao", "Instituição de teste");
        v.put("paragrafos", List.of("Declara-se que Maria <Lopes> & Filhos exerce funções.", "Segundo parágrafo."));
        v.put("finalidade", "efeitos de crédito bancário");
        v.put("numero", "DEC-2026-000001");
        v.put("codigo", "ABCDEFGH23");
        v.put("dataEmissao", "25/09/2026");
        v.put("urlVerificacao", "/api/v1/rh/verificacao/documentos/ABCDEFGH23");
        return v;
    }

    @Test
    void preencheEscapandoOTexto() {
        String html = gerador.preencher("declaracao", valores());
        assertTrue(html.contains("Maria &lt;Lopes&gt; &amp; Filhos"), html);
        assertTrue(html.contains("DEC-2026-000001"));
        assertTrue(html.contains("para efeitos de crédito bancário"));
        assertFalse(html.contains("th:text"), "os atributos do Thymeleaf nao ficam no HTML final");
    }

    @Test
    void geraUmPdf() {
        byte[] pdf = gerador.pdf(gerador.preencher("declaracao", valores()));
        assertTrue(pdf.length > 1000);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void semFinalidadeUsaAFraseGeral() {
        var v = valores();
        v.put("finalidade", null);
        assertTrue(gerador.preencher("declaracao", v).contains("Por ser verdade, se passa a presente declaração"));
    }
}

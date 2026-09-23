package cv.igrp.RH_Service.parametrizacoes.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Os parametros do mapa de ferias (BR-FER-20): datas MM-dd validas todos os anos, a preferencia
 * antes do mapa, a janela de fixacao dentro do ano, e os valores da lei quando nao ha linha.
 */
class ParametroFeriasTest {

    private static ParametroFerias criar(String preferencia, String mapa, String inicio, String fim, Integer minimo) {
        return ParametroFerias.criar(2027, preferencia, mapa, inicio, fim, minimo, "Diploma X");
    }

    private static void assert422(Runnable r) {
        var e = assertThrows(IgrpResponseStatusException.class, r::run);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getStatusCode().value());
    }

    @Test
    void criaComDatasEmTextoEDaAsDatasDoAno() {
        var p = criar("02-15", "04-15", "06-01", "09-30", 10);

        assertNotNull(p.getId());
        assertEquals(2027, p.getVigenteDesde());
        assertEquals(LocalDate.of(2028, 2, 15), p.prazoPreferencia(2028));
        assertEquals(LocalDate.of(2028, 4, 15), p.prazoMapa(2028));
        assertEquals(LocalDate.of(2028, 6, 1), p.fixacaoInicio(2028));
        assertEquals(LocalDate.of(2028, 9, 30), p.fixacaoFim(2028));
        assertEquals(10, p.getPeriodoMinimoInterpolado());
        assertEquals("02-15", ParametroFerias.texto(p.getPrazoPreferencia()));
    }

    @Test
    void daLeiSaoOsValoresDoDecretoESemId() {
        var p = ParametroFerias.daLei();

        assertTrue(p.isDaLei());
        assertNull(p.getId());
        assertEquals(LocalDate.of(2027, 1, 31), p.prazoPreferencia(2027));
        assertEquals(LocalDate.of(2027, 3, 31), p.prazoMapa(2027));
        assertEquals(LocalDate.of(2027, 5, 1), p.fixacaoInicio(2027));
        assertEquals(LocalDate.of(2027, 10, 31), p.fixacaoFim(2027));
        assertEquals(11, p.getPeriodoMinimoInterpolado());
    }

    @Test
    void datasMalEscritasOuInexistentesSao422() {
        assert422(() -> criar("1-31", "03-31", "05-01", "10-31", 11));
        assert422(() -> criar("31/01", "03-31", "05-01", "10-31", 11));
        assert422(() -> criar("02-30", "03-31", "05-01", "10-31", 11));
        assert422(() -> criar("13-01", "03-31", "05-01", "10-31", 11));
        assert422(() -> criar(null, "03-31", "05-01", "10-31", 11));
    }

    @Test
    void vinteENoveDeFevereiroNaoPorqueNaoHaTodosOsAnos() {
        assert422(() -> criar("02-29", "03-31", "05-01", "10-31", 11));
    }

    @Test
    void preferenciaDepoisDoMapaE422() {
        assert422(() -> criar("04-01", "03-31", "05-01", "10-31", 11));
    }

    @Test
    void janelaDeFixacaoAoContrarioE422() {
        assert422(() -> criar("01-31", "03-31", "11-01", "10-31", 11));
    }

    @Test
    void anoEPeriodoMinimoObrigatorios() {
        assert422(() -> ParametroFerias.criar(null, "01-31", "03-31", "05-01", "10-31", 11, null));
        assert422(() -> ParametroFerias.criar(210, "01-31", "03-31", "05-01", "10-31", 11, null));
        assert422(() -> criar("01-31", "03-31", "05-01", "10-31", null));
        assert422(() -> criar("01-31", "03-31", "05-01", "10-31", 0));
    }

    @Test
    void fundamentoEmBrancoFicaNulo() {
        var p = ParametroFerias.criar(2027, "01-31", "03-31", "05-01", "10-31", 11, "   ");
        assertNull(p.getFundamento());
    }
}

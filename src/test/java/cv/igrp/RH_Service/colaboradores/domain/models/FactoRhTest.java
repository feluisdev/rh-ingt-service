package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** BR-FAC-01..03: imutavel, competencia pelo mes da data de efeito, ajuste de mes anterior. */
class FactoRhTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 25, 10, 0);
    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();

    @Test
    void competenciaPorOmissaoEOMesDaDataDeEfeito() {
        var f = FactoRh.registar(pessoa, TipoFactoRh.ADMISSAO, LocalDate.of(2026, 9, 1), null, null, null, "x", null, AGORA);
        assertEquals(YearMonth.of(2026, 9), f.getMesCompetencia());
        assertFalse(f.isAjusteDeMesAnterior());
    }

    @Test
    void mesFechadoEntraComoAjusteNoSeguinte() {
        var f = FactoRh.registar(pessoa, TipoFactoRh.PROGRESSAO, LocalDate.of(2026, 8, 20), YearMonth.of(2026, 9),
                null, null, "x", null, AGORA);
        assertTrue(f.isAjusteDeMesAnterior());
    }

    @Test
    void osDadosFicamEmTextoSemNulosENaoMudam() {
        Map<String, Object> dados = new HashMap<>();
        dados.put("escalao", 3);
        dados.put("nulo", null);
        var f = FactoRh.registar(pessoa, TipoFactoRh.PROGRESSAO, LocalDate.of(2026, 9, 1), null, null, null, "x", dados, AGORA);
        assertEquals(Map.of("escalao", "3"), f.getDados());
        assertThrows(UnsupportedOperationException.class, () -> f.getDados().put("a", "b"));
    }

    @Test
    void semDataDeEfeitoNaoHaFacto() {
        assertThrows(NullPointerException.class,
                () -> FactoRh.registar(pessoa, TipoFactoRh.CESSACAO, null, null, null, null, "x", null, AGORA));
    }
}

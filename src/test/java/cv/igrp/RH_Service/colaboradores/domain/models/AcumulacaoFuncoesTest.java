package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** BR-ACU: casos das públicas remuneradas, terço da docência, declaração nas privadas, quem autoriza, caducidade. */
class AcumulacaoFuncoesTest {

    private static final LocalDate D = LocalDate.of(2026, 10, 1);
    private final FuncionarioId f = FuncionarioId.gerarNovo();

    private AcumulacaoFuncoes publica(AcumulacaoFuncoes.CasoPublico caso, boolean remunerada, Integer horas, Integer minutosPrincipal) {
        return AcumulacaoFuncoes.pedir(f, AcumulacaoFuncoes.Tipo.PUBLICA, caso, remunerada, "Universidade de Cabo Verde", "Docente", null, horas,
                D, D.plusMonths(9), false, minutosPrincipal);
    }

    @Test
    void publicaRemuneradaSoNosCasosDaLeiEDocenciaAteUmTerco() {
        assertThrows(IgrpResponseStatusException.class, () -> publica(null, true, null, null));
        publica(null, false, null, null);
        // 40 h/semana = 2400 min: um terço são 13 h.
        assertThrows(IgrpResponseStatusException.class, () -> publica(AcumulacaoFuncoes.CasoPublico.DOCENCIA_INVESTIGACAO, true, 14, 2400));
        var a = publica(AcumulacaoFuncoes.CasoPublico.DOCENCIA_INVESTIGACAO, true, 13, 2400);
        assertEquals(AcumulacaoFuncoes.Autorizacao.MEMBROS_GOVERNO, a.autorizacao());
        assertThrows(IgrpResponseStatusException.class, () -> publica(AcumulacaoFuncoes.CasoPublico.DOCENCIA_INVESTIGACAO, true, null, 2400));
    }

    @Test
    void privadaExigeADeclaracao() {
        assertThrows(IgrpResponseStatusException.class, () -> AcumulacaoFuncoes.pedir(f, AcumulacaoFuncoes.Tipo.PRIVADA, null, true, "Escritório X",
                "Consultoria", null, null, D, null, false, null));
        var a = AcumulacaoFuncoes.pedir(f, AcumulacaoFuncoes.Tipo.PRIVADA, null, false, "Associação Y", "Voluntariado", "Sábados", null, D, null,
                true, null);
        assertEquals(AcumulacaoFuncoes.Autorizacao.DIRIGENTE_MAXIMO, a.autorizacao());
    }

    @Test
    void autorizarCaducarECessar() {
        var a = publica(null, false, null, null);
        assertThrows(IgrpResponseStatusException.class, () -> a.autorizar(" ", D));
        a.autorizar("Despacho 3/2026", D);
        assertTrue(a.emVigor(D.plusDays(10)));
        assertFalse(a.caducarSeTerminou(D.plusMonths(9)));
        assertTrue(a.caducarSeTerminou(D.plusMonths(9).plusDays(1)));
        assertEquals(AcumulacaoFuncoes.Estado.CADUCADA, a.getEstado());
        var b = publica(null, false, null, null);
        assertThrows(IgrpResponseStatusException.class, () -> b.cessar(D, null));
        b.autorizar("D", D);
        b.cessar(D.plusDays(30), "Incompatível com o novo cargo");
        assertEquals(AcumulacaoFuncoes.Estado.CESSADA, b.getEstado());
    }
}

package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** BR-EXO: pré-aviso de 60 dias; efeitos na data pretendida, ou quando a condição cessa, ou no máximo aos 90 dias. */
class ExoneracaoTest {

    private static final LocalDate AVISO = LocalDate.of(2026, 3, 1);

    private Exoneracao deferida(LocalDate pretendida) {
        var e = Exoneracao.pedir(FuncionarioId.gerarNovo(), AVISO, pretendida, "Mudança de país", true, AVISO);
        e.deferir("Despacho 7/2026", AVISO.plusDays(10));
        return e;
    }

    @Test
    void preAvisoDe60Dias() {
        var e = Exoneracao.pedir(FuncionarioId.gerarNovo(), AVISO, null, null, true, AVISO);
        assertEquals(AVISO.plusDays(60), e.getDataPretendida());
        assertThrows(IgrpResponseStatusException.class,
                () -> Exoneracao.pedir(FuncionarioId.gerarNovo(), AVISO, AVISO.plusDays(59), null, true, AVISO));
        assertThrows(IgrpResponseStatusException.class,
                () -> Exoneracao.pedir(FuncionarioId.gerarNovo(), AVISO.plusDays(1), null, null, true, AVISO));
    }

    @Test
    void semCondicaoNaDataPretendida() {
        var e = deferida(AVISO.plusDays(70));
        assertNull(e.efeitoDevido(false, AVISO.plusDays(69)));
        assertEquals(AVISO.plusDays(70), e.efeitoDevido(false, AVISO.plusDays(72)));
    }

    @Test
    void condicionadaAteACausaCessarOuAos90Dias() {
        var e = deferida(null);
        e.registarCondicionada(AVISO.plusDays(75));
        assertNull(e.efeitoDevido(true, AVISO.plusDays(75)));
        // A causa cessou a seguir ao dia 75: efeitos no dia 76.
        assertEquals(AVISO.plusDays(76), e.efeitoDevido(false, AVISO.plusDays(76)));
        var f = deferida(null);
        assertEquals(AVISO.plusDays(90), f.efeitoDevido(true, AVISO.plusDays(90)));
    }

    @Test
    void desistenciaENaoRepete() {
        var e = deferida(null);
        e.desistir();
        assertThrows(IgrpResponseStatusException.class, e::desistir);
        assertThrows(IgrpResponseStatusException.class, () -> e.efectivar(AVISO.plusDays(60)));
    }
}

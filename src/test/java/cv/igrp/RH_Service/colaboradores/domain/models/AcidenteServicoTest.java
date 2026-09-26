package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** BR-SST: participação, qualificação, incapacidades sem sobreposição, alta, incapacidade permanente e encerramento. */
class AcidenteServicoTest {

    private static final LocalDate D = LocalDate.of(2026, 9, 1);

    private AcidenteServico acidente() {
        return AcidenteServico.participar(FuncionarioId.gerarNovo(), AcidenteServico.Tipo.ACIDENTE_SERVICO, D.atTime(10, 30), "Arquivo",
                "Queda da escada", "Ana", D.plusDays(1), true, D.plusDays(1));
    }

    @Test
    void participacaoValida() {
        assertThrows(IgrpResponseStatusException.class, () -> AcidenteServico.participar(FuncionarioId.gerarNovo(),
                AcidenteServico.Tipo.ACIDENTE_SERVICO, D.plusDays(2).atTime(9, 0), null, "X", null, null, false, D));
        assertThrows(IgrpResponseStatusException.class, () -> AcidenteServico.participar(FuncionarioId.gerarNovo(),
                AcidenteServico.Tipo.ACIDENTE_SERVICO, D.atTime(9, 0), null, " ", null, null, false, D));
    }

    @Test
    void qualificacaoEIncapacidades() {
        var a = acidente();
        assertFalse(a.contaComoAcidente());
        a.registarIncapacidade(null, D, null);
        assertThrows(IgrpResponseStatusException.class, () -> a.registarIncapacidade(null, D.plusDays(5), D.plusDays(6)));
        assertThrows(IgrpResponseStatusException.class, () -> a.qualificar(false, null, " "));
        a.qualificar(true, "Despacho 4/2026", null);
        assertTrue(a.contaComoAcidente());
        assertThrows(IgrpResponseStatusException.class, a::encerrar);
        a.darAlta(D.plusDays(10));
        assertEquals(D.plusDays(9), a.getIncapacidades().get(0).fim());
        a.registarIncapacidade(AcidenteServico.TipoIncapacidade.TEMPORARIA_PARCIAL, D.plusDays(10), D.plusDays(10));
        a.encerrar();
        assertEquals(AcidenteServico.Estado.ENCERRADO, a.getEstado());
        assertTrue(a.contaComoAcidente());
    }

    @Test
    void incapacidadePermanenteDaAposentacaoQuandoAbsolutaOuImpedeFuncoes() {
        var a = acidente();
        assertThrows(IgrpResponseStatusException.class, () -> a.registarIncapacidadePermanente(BigDecimal.TEN, false, false));
        a.qualificar(true, "D", null);
        assertFalse(a.registarIncapacidadePermanente(BigDecimal.valueOf(20), false, false));
        assertTrue(a.registarIncapacidadePermanente(BigDecimal.valueOf(40), false, true));
        assertTrue(a.registarIncapacidadePermanente(BigDecimal.valueOf(100), true, false));
        assertThrows(IgrpResponseStatusException.class, () -> a.registarIncapacidadePermanente(BigDecimal.valueOf(120), true, false));
    }

    @Test
    void naoQualificadoNaoRecebeIncapacidades() {
        var a = acidente();
        a.qualificar(false, null, "Ocorreu fora do serviço, por motivo pessoal");
        assertThrows(IgrpResponseStatusException.class, () -> a.registarIncapacidade(null, D, D.plusDays(2)));
        assertFalse(a.contaComoAcidente());
    }
}

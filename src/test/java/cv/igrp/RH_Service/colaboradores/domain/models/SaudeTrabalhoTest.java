package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** BR-SST: validade dos exames pela idade, restrições, reavaliação; parecer da junta. */
class SaudeTrabalhoTest {

    private static final LocalDate D = LocalDate.of(2026, 9, 1);
    private final FuncionarioId f = FuncionarioId.gerarNovo();

    @Test
    void validadePorOmissaoPelaIdade() {
        assertEquals(D.plusYears(2), ExameSaude.registar(f, ExameSaude.Tipo.PERIODICO, D, null, ExameSaude.Resultado.APTO, null, null, null, 40, D).getValidadeAte());
        assertEquals(D.plusYears(1), ExameSaude.registar(f, ExameSaude.Tipo.PERIODICO, D, null, ExameSaude.Resultado.APTO, null, null, null, 55, D).getValidadeAte());
        assertNull(ExameSaude.registar(f, ExameSaude.Tipo.OCASIONAL, D, null, ExameSaude.Resultado.INAPTO_DEFINITIVO, null, null, null, 55, D).getValidadeAte());
    }

    @Test
    void restricoesEReavaliacaoObrigatorias() {
        assertThrows(IgrpResponseStatusException.class,
                () -> ExameSaude.registar(f, ExameSaude.Tipo.PERIODICO, D, null, ExameSaude.Resultado.APTO_CONDICIONADO, " ", null, null, 40, D));
        assertThrows(IgrpResponseStatusException.class,
                () -> ExameSaude.registar(f, ExameSaude.Tipo.PERIODICO, D, null, ExameSaude.Resultado.INAPTO_TEMPORARIO, null, null, null, 40, D));
        var e = ExameSaude.registar(f, ExameSaude.Tipo.PERIODICO, D, null, ExameSaude.Resultado.INAPTO_TEMPORARIO, null, D.plusMonths(2), null, 40, D);
        assertFalse(e.apto());
        assertFalse(e.validoEm(D.plusMonths(3)));
        assertThrows(IgrpResponseStatusException.class,
                () -> ExameSaude.registar(f, ExameSaude.Tipo.PERIODICO, D.plusDays(1), null, ExameSaude.Resultado.APTO, null, null, null, 40, D));
    }

    @Test
    void parecerDaJunta() {
        var j = JuntaMedica.pedir(f, JuntaMedica.Motivo.DOENCA_PROLONGADA, "45 dias de baixa", D);
        assertThrows(IgrpResponseStatusException.class, () -> j.registarParecer(D.plusDays(5), JuntaMedica.Parecer.INCAPAZ_TEMPORARIO, null, null, D.plusDays(10)));
        assertThrows(IgrpResponseStatusException.class, () -> j.registarParecer(D.minusDays(1), JuntaMedica.Parecer.APTO, null, null, D.plusDays(10)));
        j.registarParecer(D.plusDays(5), JuntaMedica.Parecer.INCAPAZ_TEMPORARIO, 60, null, D.plusDays(10));
        assertEquals(JuntaMedica.Estado.REALIZADA, j.getEstado());
        assertThrows(IgrpResponseStatusException.class, j::cancelar);
    }
}

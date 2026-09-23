package cv.igrp.RH_Service.parametrizacoes.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import org.junit.jupiter.api.Test;

/** A contagem de dias no catálogo (V56): parametrizável, e sem partir quem não a conhece. */
class LeaveTypeContagemTest {

    private static LeaveType criar(ContagemDias contagem) {
        return LeaveType.criar("LUTO", "Luto", false, false, null, 8, null, null,
                RegimeAusencia.FALTA, EfeitoNaRemuneracao.SEM_PERDA, contagem);
    }

    @Test
    void umTipoNovoSemContagemContaEmDiasUteis_comoSempre() {
        assertEquals(ContagemDias.DIAS_UTEIS, criar(null).getContagem());
    }

    @Test
    void aInstituicaoClassificaALinha() {
        assertEquals(ContagemDias.DIAS_SEGUIDOS, criar(ContagemDias.DIAS_SEGUIDOS).getContagem());
    }

    @Test
    void oPutQueNaoAEnviaMantemAQueEstava() {
        var luto = criar(ContagemDias.DIAS_SEGUIDOS);

        luto.atualizar("Luto", false, false, null, 8, null, null, null, null, null);

        assertEquals(ContagemDias.DIAS_SEGUIDOS, luto.getContagem());
    }

    @Test
    void valorForaDaLeiE422_eEmBrancoENulo() {
        var e = assertThrows(IgrpResponseStatusException.class, () -> ContagemDias.de("DIAS_CORRIDOS"));
        assertEquals(422, e.getBody().getStatus());
        assertNull(ContagemDias.de(" "));
        assertEquals(ContagemDias.DIAS_SEGUIDOS, ContagemDias.de("dias_seguidos"));
    }
}

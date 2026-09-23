package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Regista-se o que aconteceu, e nunca se apaga: anula-se, com motivo. */
class MarcacaoAssiduidadeTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 23, 15, 0);

    private static MarcacaoAssiduidade registar(LocalDateTime momento) {
        return MarcacaoAssiduidade.registar(FuncionarioId.gerarNovo(), momento, SentidoMarcacao.ENTRADA,
                OrigemMarcacao.MANUAL, null, null, AGORA);
    }

    @Test
    void osSegundosNaoContam() {
        assertEquals(LocalDateTime.of(2026, 9, 23, 8, 3), registar(LocalDateTime.of(2026, 9, 23, 8, 3, 41)).getMomento());
    }

    @Test
    void noFuturoOuSemSentidoE422() {
        var e = assertThrows(IgrpResponseStatusException.class, () -> registar(AGORA.plusMinutes(1)));
        assertEquals(422, e.getStatusCode().value());
        assertThrows(IgrpResponseStatusException.class, () -> MarcacaoAssiduidade.registar(FuncionarioId.gerarNovo(),
                AGORA, null, OrigemMarcacao.MANUAL, null, null, AGORA));
    }

    @Test
    void anularExigeMotivoEUmaVezSo() {
        var m = registar(AGORA.minusHours(2));

        var semMotivo = assertThrows(IgrpResponseStatusException.class, () -> m.anular(" ", AGORA));
        assertEquals(422, semMotivo.getStatusCode().value());

        m.anular("picou por engano", AGORA);
        assertTrue(m.isAnulada());
        assertEquals("picou por engano", m.getMotivoAnulacao());

        var outraVez = assertThrows(IgrpResponseStatusException.class, () -> m.anular("de novo", AGORA));
        assertEquals(409, outraVez.getStatusCode().value());
    }
}

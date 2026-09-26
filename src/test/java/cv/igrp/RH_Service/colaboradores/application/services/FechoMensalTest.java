package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.FechoMensal;
import cv.igrp.RH_Service.colaboradores.domain.repository.FechoMensalRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Set;

import org.junit.jupiter.api.Test;

/** BR-FEC: fechar, reabrir com motivo; um facto com efeito num mês fechado entra no primeiro mês aberto a seguir. */
class FechoMensalTest {

    @Test
    void competenciaSaltaOsMesesFechados() {
        var repo = mock(FechoMensalRepository.class);
        when(repo.mesesFechadosDesde(any())).thenReturn(Set.of(YearMonth.of(2026, 7), YearMonth.of(2026, 8)));
        var c = new CompetenciaSalarial(repo);
        assertEquals(YearMonth.of(2026, 9), c.competencia(LocalDate.of(2026, 7, 15)));
        assertEquals(YearMonth.of(2026, 6), new CompetenciaSalarial(mock(FechoMensalRepository.class)).competencia(LocalDate.of(2026, 6, 1)));
        assertEquals(YearMonth.of(2026, 6), new CompetenciaSalarial(null).competencia(LocalDate.of(2026, 6, 1)));
    }

    @Test
    void fecharReabrirEFecharDeNovo() {
        var agora = LocalDateTime.of(2026, 9, 2, 10, 0);
        assertThrows(IgrpResponseStatusException.class, () -> FechoMensal.fechar(YearMonth.of(2026, 10), YearMonth.of(2026, 9), "[]", 0, agora));
        var f = FechoMensal.fechar(YearMonth.of(2026, 8), YearMonth.of(2026, 9), "[]", 12, agora);
        assertThrows(IgrpResponseStatusException.class, () -> f.fecharDeNovo("[]", 1, agora));
        assertThrows(IgrpResponseStatusException.class, () -> f.reabrir(" ", agora));
        f.reabrir("Faltava uma progressão", agora.plusDays(1));
        assertEquals(FechoMensal.Estado.REABERTO, f.getEstado());
        f.fecharDeNovo("[]", 13, agora.plusDays(2));
        assertEquals(2, f.getFechos());
        assertEquals(13, f.getTotalFactos());
    }
}

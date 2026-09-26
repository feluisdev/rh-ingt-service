package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/** BR-MSS: dias de ajudas de custo pelas horas de partida e regresso [ind.], estados e relatório. */
public class MissaoServicoTest {

    private static final LocalDate D = LocalDate.of(2026, 10, 5);

    private static BigDecimal dias(int diaP, int horaP, int diaR, int horaR) {
        return MissaoServico.diasAjudasCusto(D.plusDays(diaP).atTime(horaP, 0), D.plusDays(diaR).atTime(horaR, 0));
    }

    @Test
    void diasPelasHorasDePartidaERegresso() {
        assertEquals(new BigDecimal("3"), dias(0, 8, 2, 18));
        assertEquals(new BigDecimal("2.5"), dias(0, 15, 2, 18));
        assertEquals(new BigDecimal("2.0"), dias(0, 15, 2, 11));
        assertEquals(new BigDecimal("1.5"), dias(0, 8, 1, 12));
        assertEquals(new BigDecimal("0.5"), dias(0, 8, 0, 18));
        assertEquals(BigDecimal.ZERO, dias(0, 14, 0, 18));
    }

    @Test
    void ciclo() {
        var m = MissaoServico.pedir(List.of(FuncionarioId.gerarNovo()), MissaoServico.Destino.NACIONAL, "São Vicente, Mindelo", "Auditoria",
                D.atTime(7, 0), D.plusDays(2).atTime(19, 0), MissaoServico.Transporte.AVIAO, false, true, null);
        assertThrows(IgrpResponseStatusException.class, () -> m.recusar(" "));
        assertThrows(IgrpResponseStatusException.class, () -> m.realizar("R", null, null, D.plusDays(5)));
        m.autorizar("Despacho 12/2026");
        assertThrows(IgrpResponseStatusException.class, () -> m.realizar("R", null, null, D.plusDays(1)));
        assertThrows(IgrpResponseStatusException.class, () -> m.realizar(" ", null, null, D.plusDays(5)));
        m.realizar("Auditoria feita", null, D.plusDays(3).atTime(10, 0), D.plusDays(5));
        assertEquals(new BigDecimal("3.5"), m.diasAjudasCusto());
        assertThrows(IgrpResponseStatusException.class, () -> m.cancelar("x"));
    }

    @Test
    void validaDadosEParticipantes() {
        var f = FuncionarioId.gerarNovo();
        assertThrows(IgrpResponseStatusException.class, () -> MissaoServico.pedir(List.of(f, f), MissaoServico.Destino.NACIONAL, "X", "Y",
                D.atTime(8, 0), D.atTime(18, 0), null, false, false, null));
        assertThrows(IgrpResponseStatusException.class, () -> MissaoServico.pedir(List.of(f), MissaoServico.Destino.ESTRANGEIRO, "Lisboa", "Y",
                D.atTime(18, 0), D.atTime(8, 0), null, false, false, null));
    }
}

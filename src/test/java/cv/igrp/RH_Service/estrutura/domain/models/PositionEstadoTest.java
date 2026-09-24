package cv.igrp.RH_Service.estrutura.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Congelar e descongelar um Lugar: motivo obrigatório, extinto é terminal, idempotência. */
class PositionEstadoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 24);

    @Test
    void congelarGuardaMotivoDespachoEData() {
        Position p = lugar(Position.ATIVO);
        assertTrue(p.congelar("Sem dotação orçamental", " Desp. 12/2026 ", HOJE));
        assertEquals(Position.CONGELADO, p.getEstado());
        assertEquals("Sem dotação orçamental", p.getEstadoMotivo());
        assertEquals("Desp. 12/2026", p.getEstadoDespacho());
        assertEquals(HOJE, p.getEstadoDesde());
        assertFalse(p.podeSerOcupado());
    }

    @Test
    void descongelarVoltaAAtivoEOcupavel() {
        Position p = lugar(Position.CONGELADO);
        assertTrue(p.descongelar("Dotação reposta no OE 2027", null, HOJE));
        assertEquals(Position.ATIVO, p.getEstado());
        assertEquals("Dotação reposta no OE 2027", p.getEstadoMotivo());
        assertNull(p.getEstadoDespacho());
        assertTrue(p.podeSerOcupado());
    }

    @Test
    void semMotivoERecusado() {
        IgrpResponseStatusException e = assertThrows(IgrpResponseStatusException.class,
                () -> lugar(Position.ATIVO).congelar("  ", null, HOJE));
        assertEquals(HttpStatus.BAD_REQUEST.value(), e.getStatusCode().value());
        assertThrows(IgrpResponseStatusException.class, () -> lugar(Position.CONGELADO).descongelar(null, null, HOJE));
    }

    @Test
    void extintoNaoSeCongelaNemVolta() {
        IgrpResponseStatusException e1 = assertThrows(IgrpResponseStatusException.class,
                () -> lugar(Position.EXTINTO).congelar("x", null, HOJE));
        IgrpResponseStatusException e2 = assertThrows(IgrpResponseStatusException.class,
                () -> lugar(Position.EXTINTO).descongelar("x", null, HOJE));
        assertEquals(HttpStatus.CONFLICT.value(), e1.getStatusCode().value());
        assertEquals(HttpStatus.CONFLICT.value(), e2.getStatusCode().value());
    }

    @Test
    void jaNoEstadoPedidoNaoMexeNoMotivo() {
        Position congelado = lugar(Position.CONGELADO);
        congelado.reconstituirMotivoDoEstado("motivo antigo", null, HOJE.minusDays(10));
        assertFalse(congelado.congelar("outro", null, HOJE));
        assertEquals("motivo antigo", congelado.getEstadoMotivo());

        Position ativo = lugar(Position.ATIVO);
        assertFalse(ativo.descongelar("x", null, HOJE));
        assertNull(ativo.getEstadoMotivo());
    }

    private static Position lugar(String estado) {
        return Position.reconstituir(PositionId.gerarNovo(), "LUG-T", UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), null, null, estado, null, !Position.EXTINTO.equals(estado));
    }
}

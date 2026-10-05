package cv.igrp.RH_Service.estrutura.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.estrutura.application.dto.EstadoLugarRequestDTO;
import cv.igrp.RH_Service.estrutura.application.port.PositionOccupancyPort;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** BR-POS-06 e BR-POS-07: congelar exige motivo e Lugar sem titular; descongelar exige motivo. */
@ExtendWith(MockitoExtension.class)
class CongelarDescongelarPositionCommandHandlerTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private PositionOccupancyPort positionOccupancyPort;

    @Test
    void congelaLugarVagoComMotivo() {
        Position p = lugar(Position.ATIVO);
        when(positionRepository.findById(any())).thenReturn(Optional.of(p));
        when(positionOccupancyPort.ocupados(anyCollection())).thenReturn(Set.of());

        ResponseEntity<SuccessResponseDTO> r = congelar().handle(new CongelarPositionCommand(pedido("Sem dotação"), id(p)));

        assertTrue(r.getBody().isSucesso());
        assertEquals(Position.CONGELADO, p.getEstado());
        verify(positionRepository).save(p);
    }

    @Test
    void naoCongelaLugarComTitular() {
        Position p = lugar(Position.ATIVO);
        when(positionRepository.findById(any())).thenReturn(Optional.of(p));
        when(positionOccupancyPort.ocupados(anyCollection())).thenReturn(Set.of(p.getId().getValor()));

        IgrpResponseStatusException e = assertThrows(IgrpResponseStatusException.class,
                () -> congelar().handle(new CongelarPositionCommand(pedido("Sem dotação"), id(p))));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getStatusCode().value());
        assertEquals(Position.ATIVO, p.getEstado());
        verify(positionRepository, never()).save(any());
    }

    /** BR-AF-27: o Lugar reservado para quem aguarda o contrato não se congela sem cancelar a reserva. */
    @Test
    void naoCongelaLugarReservado() {
        Position p = lugar(Position.ATIVO);
        when(positionRepository.findById(any())).thenReturn(Optional.of(p));
        when(positionOccupancyPort.ocupados(anyCollection())).thenReturn(Set.of());
        when(positionOccupancyPort.reservados(anyCollection())).thenReturn(java.util.Map.of(p.getId().getValor(),
                new PositionOccupancyPort.Reserva(UUID.randomUUID(), "Ana")));

        IgrpResponseStatusException e = assertThrows(IgrpResponseStatusException.class,
                () -> congelar().handle(new CongelarPositionCommand(pedido("Sem dotação"), id(p))));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getStatusCode().value());
        assertTrue(e.getBody().getTitle().contains("Ana"));
        assertEquals(Position.ATIVO, p.getEstado());
        verify(positionRepository, never()).save(any());
    }

    @Test
    void congelarSemMotivoE400AntesDeProcurarOLugar() {
        IgrpResponseStatusException e = assertThrows(IgrpResponseStatusException.class,
                () -> congelar().handle(new CongelarPositionCommand(new EstadoLugarRequestDTO(), UUID.randomUUID().toString())));
        assertEquals(HttpStatus.BAD_REQUEST.value(), e.getStatusCode().value());
        verify(positionRepository, never()).findById(any());
    }

    @Test
    void congelarOJaCongeladoNaoTemEfeito() {
        Position p = lugar(Position.CONGELADO);
        when(positionRepository.findById(any())).thenReturn(Optional.of(p));

        ResponseEntity<SuccessResponseDTO> r = congelar().handle(new CongelarPositionCommand(pedido("x"), id(p)));

        assertFalse(r.getBody().isSucesso());
        verify(positionRepository, never()).save(any());
    }

    @Test
    void descongelaComMotivo() {
        Position p = lugar(Position.CONGELADO);
        when(positionRepository.findById(any())).thenReturn(Optional.of(p));

        ResponseEntity<SuccessResponseDTO> r = descongelar().handle(new DescongelarPositionCommand(pedido("Dotação reposta"), id(p)));

        assertTrue(r.getBody().isSucesso());
        assertEquals(Position.ATIVO, p.getEstado());
        verify(positionRepository).save(p);
    }

    @Test
    void descongelarOActivoNaoTemEfeitoEOExtintoE409() {
        Position activo = lugar(Position.ATIVO);
        when(positionRepository.findById(any())).thenReturn(Optional.of(activo));
        assertFalse(descongelar().handle(new DescongelarPositionCommand(pedido("x"), id(activo))).getBody().isSucesso());

        Position extinto = lugar(Position.EXTINTO);
        when(positionRepository.findById(any())).thenReturn(Optional.of(extinto));
        IgrpResponseStatusException e = assertThrows(IgrpResponseStatusException.class,
                () -> descongelar().handle(new DescongelarPositionCommand(pedido("x"), id(extinto))));
        assertEquals(HttpStatus.CONFLICT.value(), e.getStatusCode().value());
        verify(positionRepository, never()).save(any());
    }

    @Test
    void lugarInexistenteE404() {
        when(positionRepository.findById(any())).thenReturn(Optional.empty());
        IgrpResponseStatusException e = assertThrows(IgrpResponseStatusException.class,
                () -> descongelar().handle(new DescongelarPositionCommand(pedido("x"), UUID.randomUUID().toString())));
        assertEquals(HttpStatus.NOT_FOUND.value(), e.getStatusCode().value());
    }

    private CongelarPositionCommandHandler congelar() {
        return new CongelarPositionCommandHandler(positionRepository, positionOccupancyPort);
    }

    private DescongelarPositionCommandHandler descongelar() {
        return new DescongelarPositionCommandHandler(positionRepository);
    }

    private static EstadoLugarRequestDTO pedido(String motivo) {
        EstadoLugarRequestDTO d = new EstadoLugarRequestDTO();
        d.setMotivo(motivo);
        return d;
    }

    private static String id(Position p) {
        return p.getId().getStringValor();
    }

    private static Position lugar(String estado) {
        return Position.reconstituir(PositionId.gerarNovo(), "LUG-T", UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), null, null, estado, null, !Position.EXTINTO.equals(estado));
    }
}

package cv.igrp.RH_Service.estrutura.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.estrutura.application.port.PositionOccupancyPort;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

/** Extinguir um Lugar; o reservado para quem aguarda o contrato não se extingue (BR-AF-27). */
@ExtendWith(MockitoExtension.class)
class ExtinguirPositionCommandHandlerTest {

    @Mock private PositionRepository positionRepository;
    @Mock private PositionOccupancyPort positionOccupancyPort;

    @Test
    void extingueOLugar() {
        Position p = lugar();
        when(positionRepository.findById(any())).thenReturn(Optional.of(p));

        handler().handle(new ExtinguirPositionCommand(p.getId().getStringValor()));

        assertEquals(Position.EXTINTO, p.getEstado());
        verify(positionRepository).save(p);
    }

    @Test
    void naoExtingueUmLugarReservado() {
        Position p = lugar();
        when(positionRepository.findById(any())).thenReturn(Optional.of(p));
        when(positionOccupancyPort.reservados(anyCollection()))
                .thenReturn(Map.of(p.getId().getValor(), new PositionOccupancyPort.Reserva(UUID.randomUUID(), "Ana")));

        IgrpResponseStatusException e = assertThrows(IgrpResponseStatusException.class,
                () -> handler().handle(new ExtinguirPositionCommand(p.getId().getStringValor())));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getStatusCode().value());
        assertEquals(Position.ATIVO, p.getEstado());
        verify(positionRepository, never()).save(any());
    }

    private ExtinguirPositionCommandHandler handler() {
        return new ExtinguirPositionCommandHandler(positionRepository, positionOccupancyPort);
    }

    private static Position lugar() {
        return Position.reconstituir(PositionId.gerarNovo(), "LUG-T", UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), null, null, Position.ATIVO, null, true);
    }
}

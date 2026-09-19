package cv.igrp.RH_Service.colaboradores.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.dto.TransferenciaRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova de {@link TransferirColaboradorCommandHandler}: campos obrigatórios, colaborador activo
 * e a forma da resposta (Lugar de partida e de chegada). As regras da transferência estão
 * provadas em {@code AssignmentServiceTransferenciaTest}.
 */
@ExtendWith(MockitoExtension.class)
class TransferirColaboradorCommandHandlerTest {

    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private AssignmentService assignmentService;

    @InjectMocks private TransferirColaboradorCommandHandler handler;

    private final UUID funcionarioUuid = UUID.randomUUID();
    private final FuncionarioId funcionarioId = FuncionarioId.from(funcionarioUuid);
    private final UUID origemId = UUID.randomUUID();
    private final UUID destinoId = UUID.randomUUID();
    private final UUID unidadeDestinoId = UUID.randomUUID();
    private final LocalDate dataEfeito = LocalDate.of(2026, 10, 1);

    private TransferirColaboradorCommand command(String positionId, LocalDate data) {
        TransferenciaRequestDTO dto = new TransferenciaRequestDTO();
        dto.setPositionId(positionId);
        dto.setDataEfeito(data);
        dto.setDespachoNumero("88/2026");
        return new TransferirColaboradorCommand(funcionarioUuid.toString(), dto);
    }

    private void funcionarioActivo(boolean activo) {
        Funcionario funcionario = mock(Funcionario.class);
        when(funcionario.getIsActive()).thenReturn(activo);
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(funcionario));
    }

    private Position lugar(UUID id, UUID unidadeId, String numero) {
        return Position.reconstituir(PositionId.from(id), numero, UUID.randomUUID(), unidadeId,
                UUID.randomUUID(), UUID.randomUUID(), null, null, Position.ATIVO, null, true);
    }

    @Test
    void transfereEDevolve201ComOsLugares() {
        funcionarioActivo(true);
        Assignment nova = Assignment.criar(funcionarioId, destinoId, UUID.randomUUID(), null,
                TipoAfectacao.PRINCIPAL, Assignment.TRANSFERENCIA, dataEfeito, null, null);
        when(assignmentService.transferir(eq(funcionarioId), eq(destinoId), isNull(), eq(dataEfeito), any()))
                .thenReturn(new AssignmentService.Transferencia(nova,
                        lugar(origemId, UUID.randomUUID(), "L-001"),
                        lugar(destinoId, unidadeDestinoId, "L-042")));

        var response = handler.handle(command(destinoId.toString(), dataEfeito));

        assertEquals(201, response.getStatusCode().value());
        var body = response.getBody();
        assertEquals("L-001", body.getNumeroLugarAnterior());
        assertEquals("L-042", body.getNumeroLugar());
        assertEquals(destinoId.toString(), body.getPositionId());
        assertEquals(unidadeDestinoId.toString(), body.getUnidadeOrganicaId());
        assertEquals(dataEfeito, body.getDataEfeito());
        verify(assignmentService).transferir(funcionarioId, destinoId, null, dataEfeito,
                "Transferência (despacho 88/2026)");
    }

    @Test
    void recusaColaboradorInactivo() {
        funcionarioActivo(false);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(destinoId.toString(), dataEfeito)));

        assertEquals(422, ex.getStatusCode().value());
        verifyNoInteractions(assignmentService);
    }

    @Test
    void recusaPedidoSemLugarDeDestino() {
        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(null, dataEfeito)));

        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(funcionarioRepository, assignmentService);
    }

    @Test
    void recusaPedidoSemDataDeEfeito() {
        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(destinoId.toString(), null)));

        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(funcionarioRepository, assignmentService);
    }

    @Test
    void recusaUuidInvalido() {
        funcionarioActivo(true);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command("nao-e-uuid", dataEfeito)));

        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(assignmentService);
    }

    @Test
    void recusaFuncionarioInexistente() {
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(destinoId.toString(), dataEfeito)));

        assertEquals(404, ex.getStatusCode().value());
    }
}

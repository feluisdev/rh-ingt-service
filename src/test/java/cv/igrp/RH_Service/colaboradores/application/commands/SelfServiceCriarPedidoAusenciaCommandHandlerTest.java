package cv.igrp.RH_Service.colaboradores.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaCriadoResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.SelfServiceCriarPedidoAusenciaRequestDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

/**
 * O pedido feito pelo proprio (/me) segue as regras do RH: delega no caminho comum (contagem, feriados,
 * tectos, horas). Antes contava dias de calendario e nao via nada disso.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SelfServiceCriarPedidoAusenciaCommandHandlerTest {

    @Mock private CurrentEmployeeResolver currentEmployeeResolver;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private CreatePedidoAusenciaCommandHandler createPedidoAusenciaCommandHandler;

    @InjectMocks private SelfServiceCriarPedidoAusenciaCommandHandler handler;

    private final FuncionarioId eu = FuncionarioId.gerarNovo();
    private Funcionario pessoa;

    @BeforeEach
    void base() {
        pessoa = mock(Funcionario.class);
        when(pessoa.getIsActive()).thenReturn(true);
        when(currentEmployeeResolver.resolve()).thenReturn(eu);
        when(funcionarioRepository.findById(eu)).thenReturn(Optional.of(pessoa));
        when(createPedidoAusenciaCommandHandler.handle(any()))
                .thenReturn(ResponseEntity.status(201).body(new PedidoAusenciaCriadoResponseDTO("p1", 0, "PENDENTE", 60)));
    }

    private SelfServiceCriarPedidoAusenciaRequestDTO pedido(LocalDate de, LocalDate ate) {
        var dto = new SelfServiceCriarPedidoAusenciaRequestDTO();
        dto.setLeaveTypeId(UUID.randomUUID());
        dto.setStartDate(de);
        dto.setEndDate(ate);
        dto.setNotes("amamentacao");
        dto.setStartTime("08:00");
        dto.setEndTime("09:00");
        return dto;
    }

    @Test
    void delegaNoCaminhoDoRhComOsMesmosCampos() {
        var dto = pedido(LocalDate.of(2027, 3, 1), LocalDate.of(2027, 6, 1));

        var r = handler.handle(new SelfServiceCriarPedidoAusenciaCommand(null, dto));

        assertEquals(201, r.getStatusCode().value());
        assertEquals("p1", r.getBody().getId());
        var captor = ArgumentCaptor.forClass(CreatePedidoAusenciaCommand.class);
        verify(createPedidoAusenciaCommandHandler).handle(captor.capture());
        var enviado = captor.getValue();
        assertEquals(eu.getStringValor(), enviado.getFuncionarioId());
        assertEquals(dto.getLeaveTypeId().toString(), enviado.getRequest().getTipoAusenciaId());
        assertEquals(dto.getStartDate(), enviado.getRequest().getDataInicio());
        assertEquals("08:00", enviado.getRequest().getHoraInicio());
        assertEquals("09:00", enviado.getRequest().getHoraFim());
        assertEquals("amamentacao", enviado.getRequest().getMotivo());
    }

    @Test
    void colaboradorInactivoE403() {
        when(pessoa.getIsActive()).thenReturn(false);
        var e = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(
                new SelfServiceCriarPedidoAusenciaCommand(null, pedido(LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 1)))));
        assertEquals(403, e.getStatusCode().value());
        verify(createPedidoAusenciaCommandHandler, never()).handle(any());
    }

    @Test
    void fimAntesDoInicioE400() {
        var e = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(
                new SelfServiceCriarPedidoAusenciaCommand(null, pedido(LocalDate.of(2027, 3, 2), LocalDate.of(2027, 3, 1)))));
        assertEquals(400, e.getStatusCode().value());
    }
}

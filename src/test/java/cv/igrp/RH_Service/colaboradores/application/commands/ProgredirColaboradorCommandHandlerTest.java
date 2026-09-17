package cv.igrp.RH_Service.colaboradores.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.application.dto.ProgressaoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.application.services.VinculoLaboralService;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.VinculoLaboral;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova das validações de elegibilidade de {@link ProgredirColaboradorCommandHandler}:
 * colaborador activo e vínculo laboral que permita progressão. A travessia até ao vínculo
 * está provada em {@code VinculoLaboralServiceTest} e a mecânica da afectação em
 * {@code AssignmentServiceProgressaoTest}.
 */
@ExtendWith(MockitoExtension.class)
class ProgredirColaboradorCommandHandlerTest {

    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private VinculoLaboralService vinculoLaboralService;
    @Mock private AssignmentService assignmentService;

    @InjectMocks private ProgredirColaboradorCommandHandler handler;

    private final UUID funcionarioUuid = UUID.randomUUID();
    private final FuncionarioId funcionarioId = FuncionarioId.from(funcionarioUuid);
    private final LocalDate dataEfeito = LocalDate.of(2026, 10, 1);

    private ProgredirColaboradorCommand command(LocalDate data) {
        ProgressaoRequestDTO dto = new ProgressaoRequestDTO();
        dto.setDataEfeito(data);
        dto.setDespachoNumero("12/2026");
        return new ProgredirColaboradorCommand(funcionarioUuid.toString(), dto);
    }

    private void funcionarioActivo(boolean activo) {
        Funcionario funcionario = mock(Funcionario.class);
        when(funcionario.getIsActive()).thenReturn(activo);
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(funcionario));
    }

    private void vinculoElegivel(boolean elegivel) {
        VinculoLaboral vinculo = mock(VinculoLaboral.class);
        when(vinculo.isEligibleForProgression()).thenReturn(elegivel);
        if (!elegivel) when(vinculo.getDescription()).thenReturn("Estagiário");
        when(vinculoLaboralService.doFuncionario(funcionarioId)).thenReturn(vinculo);
    }

    private static Grade escalao(int numero) {
        return Grade.reconstituir(GradeId.gerarNovo(), CategoryId.from(UUID.randomUUID()), numero,
                "E" + numero, "Escalão " + numero, BigDecimal.TEN, null, true);
    }

    @Test
    void progrideEDevolve201ComOsEscaloes() {
        funcionarioActivo(true);
        vinculoElegivel(true);
        Assignment nova = Assignment.criar(funcionarioId, UUID.randomUUID(), UUID.randomUUID(), null,
                Assignment.PRINCIPAL, Assignment.PROGRESSAO, dataEfeito, null, null);
        when(assignmentService.progredir(eq(funcionarioId), eq(dataEfeito), any()))
                .thenReturn(new AssignmentService.Progressao(nova, escalao(1), escalao(2)));

        var response = handler.handle(command(dataEfeito));

        assertEquals(201, response.getStatusCode().value());
        var body = response.getBody();
        assertEquals("Escalão 1", body.getEscalaoAnterior());
        assertEquals("Escalão 2", body.getEscalaoNovo());
        assertEquals(dataEfeito, body.getDataEfeito());
        assertEquals(nova.getId().getStringValor(), body.getId());
        verify(assignmentService).progredir(funcionarioId, dataEfeito, "Progressão (despacho 12/2026)");
    }

    @Test
    void recusaColaboradorInactivo() {
        funcionarioActivo(false);

        var ex = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command(dataEfeito)));

        assertEquals(422, ex.getStatusCode().value());
        verifyNoInteractions(vinculoLaboralService, assignmentService);
    }

    @Test
    void recusaVinculoNaoElegivel() {
        funcionarioActivo(true);
        vinculoElegivel(false);

        var ex = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command(dataEfeito)));

        assertEquals(422, ex.getStatusCode().value());
        verifyNoInteractions(assignmentService);
    }

    @Test
    void propagaAFalhaDeVinculoIndeterminado() {
        funcionarioActivo(true);
        when(vinculoLaboralService.doFuncionario(funcionarioId))
                .thenThrow(IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                        "O colaborador não tem contrato corrente — não é possível determinar o vínculo laboral."));

        var ex = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command(dataEfeito)));

        assertEquals(422, ex.getStatusCode().value());
        verifyNoInteractions(assignmentService);
    }

    @Test
    void recusaPedidoSemDataDeEfeito() {
        var ex = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command(null)));

        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(funcionarioRepository, vinculoLaboralService, assignmentService);
    }

    @Test
    void recusaFuncionarioInexistente() {
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());

        var ex = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command(dataEfeito)));

        assertEquals(404, ex.getStatusCode().value());
    }
}

package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.dto.CreateSiadapEvaluationRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.SiadapConfigRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Primeiro teste deste handler (109-03-PLAN.md, Task 3). Todos os casos capturam o argumento
 * de {@code save} — afirmar sobre o DTO devolvido não distinguiria "derivou e gravou" de
 * "devolveu o que lhe deram".
 */
@ExtendWith(MockitoExtension.class)
class CreateSiadapEvaluationCommandHandlerTest {

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private SiadapConfigRepository configRepository;

    @Mock
    private FuncionarioLookupPort funcionarioLookupPort;

    @Mock
    private OrganicaLookupPort organicaLookupPort;

    @Mock
    private SiadapEvaluationMapper mapper;

    @InjectMocks
    private CreateSiadapEvaluationCommandHandler handler;

    private static CreateSiadapEvaluationRequestDTO requestDto(String employeeId, Integer year, String evaluatorId) {
        CreateSiadapEvaluationRequestDTO dto = new CreateSiadapEvaluationRequestDTO();
        dto.setEmployeeId(employeeId);
        dto.setYear(year);
        dto.setEvaluatorId(evaluatorId);
        return dto;
    }

    private void stubHappyPathCollaborators(String employeeId, Integer year) {
        when(evaluationRepository.findByEmployeeAndYear(employeeId, year)).thenReturn(Optional.empty());
        when(funcionarioLookupPort.findById(UUID.fromString(employeeId)))
                .thenReturn(Optional.of(new FuncionarioDTO(employeeId, "Fulano Tal")));
        when(configRepository.findByFiscalYear(year)).thenReturn(Optional.empty());
        when(evaluationRepository.save(any(SiadapEvaluation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toFullDto(any(SiadapEvaluation.class))).thenReturn(new SiadapEvaluationDTO());
    }

    @Test
    void handleWithUnitHavingResponsibleDerivesEvaluator() {
        String employeeId = UUID.randomUUID().toString();
        Integer year = 2027;
        UUID unitId = UUID.randomUUID();
        UUID responsibleId = UUID.randomUUID();
        stubHappyPathCollaborators(employeeId, year);
        when(funcionarioLookupPort.findCurrentOrganizationalUnitId(UUID.fromString(employeeId)))
                .thenReturn(Optional.of(unitId));
        when(organicaLookupPort.findResponsibleEmployeeId(unitId)).thenReturn(Optional.of(responsibleId));

        handler.handle(new CreateSiadapEvaluationCommand(requestDto(employeeId, year, null)));

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository).save(captor.capture());
        assertEquals(responsibleId.toString(), captor.getValue().getEvaluatorId());
    }

    @Test
    void handleWithDivergentRequestEvaluatorIsIgnored() {
        String employeeId = UUID.randomUUID().toString();
        Integer year = 2027;
        UUID unitId = UUID.randomUUID();
        UUID responsibleId = UUID.randomUUID();
        String clientSuppliedEvaluatorId = UUID.randomUUID().toString();
        stubHappyPathCollaborators(employeeId, year);
        when(funcionarioLookupPort.findCurrentOrganizationalUnitId(UUID.fromString(employeeId)))
                .thenReturn(Optional.of(unitId));
        when(organicaLookupPort.findResponsibleEmployeeId(unitId)).thenReturn(Optional.of(responsibleId));

        handler.handle(new CreateSiadapEvaluationCommand(requestDto(employeeId, year, clientSuppliedEvaluatorId)));

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository).save(captor.capture());
        assertEquals(responsibleId.toString(), captor.getValue().getEvaluatorId());
    }

    @Test
    void handleWithUnitHavingNoResponsibleDoesNotBlock_D05() {
        String employeeId = UUID.randomUUID().toString();
        Integer year = 2027;
        UUID unitId = UUID.randomUUID();
        stubHappyPathCollaborators(employeeId, year);
        when(funcionarioLookupPort.findCurrentOrganizationalUnitId(UUID.fromString(employeeId)))
                .thenReturn(Optional.of(unitId));
        when(organicaLookupPort.findResponsibleEmployeeId(unitId)).thenReturn(Optional.empty());

        handler.handle(new CreateSiadapEvaluationCommand(requestDto(employeeId, year, null)));

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository).save(captor.capture());
        assertNull(captor.getValue().getEvaluatorId());
    }

    @Test
    void handleWithNoCurrentEnquadramentoDoesNotBlockAndShortCircuitsBeforeOrganicaLookup() {
        String employeeId = UUID.randomUUID().toString();
        Integer year = 2027;
        stubHappyPathCollaborators(employeeId, year);
        when(funcionarioLookupPort.findCurrentOrganizationalUnitId(UUID.fromString(employeeId)))
                .thenReturn(Optional.empty());

        handler.handle(new CreateSiadapEvaluationCommand(requestDto(employeeId, year, null)));

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository).save(captor.capture());
        assertNull(captor.getValue().getEvaluatorId());
        verifyNoInteractions(organicaLookupPort);
    }

    @Test
    void handleWithExistingEvaluationForEmployeeAndYearIsRejected() {
        String employeeId = UUID.randomUUID().toString();
        Integer year = 2027;
        SiadapEvaluation existing = SiadapEvaluation.create(employeeId, year, null, null,
                new java.math.BigDecimal("60"), new java.math.BigDecimal("40"));
        when(evaluationRepository.findByEmployeeAndYear(employeeId, year)).thenReturn(Optional.of(existing));

        CreateSiadapEvaluationCommand command = new CreateSiadapEvaluationCommand(requestDto(employeeId, year, null));

        IgrpResponseStatusException exception =
                assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals(400, exception.getBody().getStatus());
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void handleWithUnknownEmployeeIsRejected() {
        String employeeId = UUID.randomUUID().toString();
        Integer year = 2027;
        when(evaluationRepository.findByEmployeeAndYear(employeeId, year)).thenReturn(Optional.empty());
        when(funcionarioLookupPort.findById(UUID.fromString(employeeId))).thenReturn(Optional.empty());

        CreateSiadapEvaluationCommand command = new CreateSiadapEvaluationCommand(requestDto(employeeId, year, null));

        IgrpResponseStatusException exception =
                assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals(400, exception.getBody().getStatus());
        verify(evaluationRepository, never()).save(any());
    }
}

package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.dto.NegotiateSiadapObjectivesRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.IndividualObjective;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class NegotiateSiadapObjectivesCommandHandlerTest {

    private static final Integer YEAR = 2026;

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private SiadapEvaluationMapper mapper;

    @Mock
    private CurrentEmployeeResolver currentEmployeeResolver;

    @InjectMocks
    private NegotiateSiadapObjectivesCommandHandler handler;

    private SiadapEvaluation buildPendingAcceptanceEvaluation() {
        SiadapEvaluation created = SiadapEvaluation.create(
                UUID.randomUUID().toString(),
                YEAR,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                new BigDecimal("60"),
                new BigDecimal("40"));

        List<IndividualObjective> objectives = List.of(
                IndividualObjective.create("OBJ-1", "Descrição do objetivo 1", "Indicador 1", new BigDecimal("100"), new BigDecimal("40")),
                IndividualObjective.create("OBJ-2", "Descrição do objetivo 2", "Indicador 2", new BigDecimal("100"), new BigDecimal("30")),
                IndividualObjective.create("OBJ-3", "Descrição do objetivo 3", "Indicador 3", new BigDecimal("100"), new BigDecimal("30")));

        return created.contractualizeObjectives(objectives);
    }

    @Test
    void negotiatesFromPendingAcceptanceWithoutAdvancingPhaseAndAcceptsEmptyComment() {
        SiadapEvaluation evaluation = buildPendingAcceptanceEvaluation();
        SiadapEvaluationId evalId = evaluation.getId();

        when(evaluationRepository.findById(any(SiadapEvaluationId.class)))
                .thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(any(SiadapEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toEntity(any(SiadapEvaluation.class)))
                .thenReturn(new SiadapEvaluationEntity());
        when(mapper.toDto(any(SiadapEvaluationEntity.class)))
                .thenReturn(new SiadapEvaluationDTO());
        when(currentEmployeeResolver.resolve())
                .thenReturn(FuncionarioId.from(evaluation.getEmployeeId()));

        // Empty/null comment must be accepted — comment is OPTIONAL for SIADAP (Pitfall 5).
        NegotiateSiadapObjectivesRequestDTO body = new NegotiateSiadapObjectivesRequestDTO();
        NegotiateSiadapObjectivesCommand command = new NegotiateSiadapObjectivesCommand(evalId.getStringValor(), body);

        ResponseEntity<SiadapEvaluationDTO> response = handler.handle(command);

        assertEquals(200, response.getStatusCode().value());

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository, times(1)).save(captor.capture());

        SiadapEvaluation saved = captor.getValue();
        assertEquals(AcceptanceStatus.NEGOTIATING, saved.getAcceptanceStatus());
        assertEquals(EvaluationPhase.OPEN, saved.getPhase());
    }

    @Test
    void throwsNotFoundWhenEvaluationDoesNotExist() {
        SiadapEvaluationId evalId = SiadapEvaluationId.gerarNovo();

        when(evaluationRepository.findById(any(SiadapEvaluationId.class)))
                .thenReturn(Optional.empty());

        NegotiateSiadapObjectivesCommand command = new NegotiateSiadapObjectivesCommand(
                evalId.getStringValor(), new NegotiateSiadapObjectivesRequestDTO());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(404, exception.getBody().getStatus());

        verify(evaluationRepository, never()).save(any());
    }

    // WR-01: only the avaliado (evaluation.employeeId) may request negotiation.
    @Test
    void throwsForbiddenWhenCurrentUserIsNotTheEmployee() {
        SiadapEvaluation evaluation = buildPendingAcceptanceEvaluation();
        SiadapEvaluationId evalId = evaluation.getId();

        when(evaluationRepository.findById(any(SiadapEvaluationId.class)))
                .thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve())
                .thenReturn(FuncionarioId.gerarNovo());

        NegotiateSiadapObjectivesCommand command = new NegotiateSiadapObjectivesCommand(
                evalId.getStringValor(), new NegotiateSiadapObjectivesRequestDTO());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(403, exception.getBody().getStatus());

        verify(evaluationRepository, never()).save(any());
    }
}

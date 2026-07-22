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
import cv.igrp.RH_Service.sigdi.application.constants.CompetencyCategory;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.FinalizeEvaluationRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.CompetencyItem;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.IndividualObjective;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * BLOQ-04: PRAZO-03 fail-closed deadline enforcement for evaluation finalization, plus WR-01
 * actor authorization (only the avaliador desta avaliação pode finalizar).
 */
@ExtendWith(MockitoExtension.class)
class FinalizeEvaluationCommandHandlerTest {

    private static final Integer YEAR = 2026;

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private SiadapEvaluationMapper mapper;

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    @Mock
    private CurrentEmployeeResolver currentEmployeeResolver;

    @InjectMocks
    private FinalizeEvaluationCommandHandler handler;

    /** Builds an evaluation already in MANAGER_EVALUATION with all objectives/competencies
     * evaluated — the exact precondition {@code finalizeEvaluation()} requires to succeed. */
    private SiadapEvaluation buildFinalizableEvaluation() {
        IndividualObjective objective = IndividualObjective.reconstruct(
                "OBJ-1", "Descrição do objetivo 1", "Indicador 1",
                new BigDecimal("100"), new BigDecimal("100"), 3, new BigDecimal("100"));
        CompetencyItem competency = CompetencyItem.reconstruct(
                "COMP-1", "Competência 1", CompetencyCategory.BEHAVIORAL, 3);

        return SiadapEvaluation.reconstruct(
                SiadapEvaluationId.gerarNovo(),
                UUID.randomUUID().toString(),
                YEAR,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                List.of(objective),
                List.of(competency),
                new BigDecimal("60"), new BigDecimal("40"),
                null, null, null, false,
                EvaluationPhase.MANAGER_EVALUATION, AcceptanceStatus.ACCEPTED, null);
    }

    @Test
    void handleWithActiveFinalPeriodSucceeds() {
        SiadapEvaluation evaluation = buildFinalizableEvaluation();

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(evaluation.getEvaluatorId()));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP_FINAL))
                .thenReturn(Optional.of(org.mockito.Mockito.mock(PaaSubmissionPeriod.class)));
        when(evaluationRepository.save(any(SiadapEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toFullDto(any(SiadapEvaluation.class))).thenReturn(new SiadapEvaluationDTO());

        FinalizeEvaluationRequestDTO body = new FinalizeEvaluationRequestDTO(evaluation.getId().getStringValor());
        FinalizeEvaluationCommand command = new FinalizeEvaluationCommand(body);

        ResponseEntity<SiadapEvaluationDTO> response = handler.handle(command);

        assertEquals(200, response.getStatusCode().value());
        verify(evaluationRepository, times(1)).save(any(SiadapEvaluation.class));
    }

    @Test
    void throwsForbiddenWhenCurrentUserIsNotTheEvaluator() {
        SiadapEvaluation evaluation = buildFinalizableEvaluation();

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.gerarNovo());

        FinalizeEvaluationRequestDTO body = new FinalizeEvaluationRequestDTO(evaluation.getId().getStringValor());
        FinalizeEvaluationCommand command = new FinalizeEvaluationCommand(body);

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(403, exception.getBody().getStatus());
        verify(evaluationRepository, never()).save(any());
    }
}

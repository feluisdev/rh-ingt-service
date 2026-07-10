package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.ContractualizeObjectivesRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.IndividualObjectiveDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class ContractualizeObjectivesCommandHandlerTest {

    private static final Integer YEAR = 2026;

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private SiadapEvaluationMapper mapper;

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    @InjectMocks
    private ContractualizeObjectivesCommandHandler handler;

    private ContractualizeObjectivesCommand buildCommand(String evaluationId) {
        // SiadapEvaluation.contractualizeObjectives requires 3-7 objectives with weights summing to 100.
        IndividualObjectiveDTO objective1 = new IndividualObjectiveDTO(
                "OBJ-1", "Descrição do objetivo 1", "Indicador 1", new BigDecimal("100"), null, null, new BigDecimal("40"));
        IndividualObjectiveDTO objective2 = new IndividualObjectiveDTO(
                "OBJ-2", "Descrição do objetivo 2", "Indicador 2", new BigDecimal("100"), null, null, new BigDecimal("30"));
        IndividualObjectiveDTO objective3 = new IndividualObjectiveDTO(
                "OBJ-3", "Descrição do objetivo 3", "Indicador 3", new BigDecimal("100"), null, null, new BigDecimal("30"));
        ContractualizeObjectivesRequestDTO body = new ContractualizeObjectivesRequestDTO(
                evaluationId, List.of(objective1, objective2, objective3));
        return new ContractualizeObjectivesCommand(body);
    }

    private SiadapEvaluation buildEvaluation(SiadapEvaluationId id) {
        return SiadapEvaluation.create(
                UUID.randomUUID().toString(),
                YEAR,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                new BigDecimal("60"),
                new BigDecimal("40"));
    }

    @Test
    void blocksWhenNoActiveSiadapIndividualPeriodExistsForTheEvaluationYear() {
        SiadapEvaluationId evalId = SiadapEvaluationId.gerarNovo();
        SiadapEvaluation evaluation = buildEvaluation(evalId);

        when(evaluationRepository.findById(any(SiadapEvaluationId.class)))
                .thenReturn(Optional.of(evaluation));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP))
                .thenReturn(Optional.empty());

        ContractualizeObjectivesCommand command = buildCommand(evalId.getStringValor());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(400, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains("Prazo não configurado para este ano"));

        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void succeedsWhenAnActiveSiadapIndividualPeriodExists() {
        SiadapEvaluationId evalId = SiadapEvaluationId.gerarNovo();
        SiadapEvaluation evaluation = buildEvaluation(evalId);

        PaaSubmissionPeriod activePeriod = PaaSubmissionPeriod.create(
                Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30), YEAR);

        when(evaluationRepository.findById(any(SiadapEvaluationId.class)))
                .thenReturn(Optional.of(evaluation));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP))
                .thenReturn(Optional.of(activePeriod));
        when(evaluationRepository.save(any(SiadapEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toFullDto(any(SiadapEvaluation.class)))
                .thenReturn(new SiadapEvaluationDTO());

        ContractualizeObjectivesCommand command = buildCommand(evalId.getStringValor());

        ResponseEntity<SiadapEvaluationDTO> response = handler.handle(command);

        assertEquals(200, response.getStatusCode().value());

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository, times(1)).save(captor.capture());

        // CONTRACT-01: proposing objectives no longer auto-advances the phase — it only
        // marks the acceptance status as pending, leaving the phase OPEN until acceptObjectives().
        SiadapEvaluation saved = captor.getValue();
        assertEquals(AcceptanceStatus.PENDING_ACCEPTANCE, saved.getAcceptanceStatus());
        assertEquals(EvaluationPhase.OPEN, saved.getPhase());
    }
}

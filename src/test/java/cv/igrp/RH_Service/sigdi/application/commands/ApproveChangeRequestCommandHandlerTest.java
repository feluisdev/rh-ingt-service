package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.ChangeRequestStatus;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.TacticalActivityStatus;
import cv.igrp.RH_Service.sigdi.application.dto.ChangeRequestResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.ChangeRequest;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.ChangeRequestRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.ChangeRequestId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * Testes do CHG-01: aprovar um pedido de alteração aplica de facto a alteração à atividade.
 *
 * <p>Antes desta fase o handler não tocava na atividade e não tinha um único teste. As duas
 * coisas andavam juntas: nenhum teste podia falhar por a alteração não ser aplicada, porque
 * nenhum teste existia.
 */
@ExtendWith(MockitoExtension.class)
class ApproveChangeRequestCommandHandlerTest {

    private static final UUID INSTITUTION_ID = UUID.randomUUID();
    private static final StrategicGoalId STRATEGIC_GOAL_ID = StrategicGoalId.from(UUID.randomUUID());
    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 12, 31);
    private static final Budget BUDGET = Budget.of(new BigDecimal("1000"), "02.02.01");

    @Mock
    private ChangeRequestRepository changeRequestRepository;

    @Mock
    private TacticalActivityRepository activityRepository;

    @InjectMocks
    private ApproveChangeRequestCommandHandler handler;

    private TacticalActivity approvedActivity() {
        TacticalActivity draft = TacticalActivity.create(
                INSTITUTION_ID, STRATEGIC_GOAL_ID, UUID.randomUUID(),
                "Atividade aprovada", "Descrição", "Justificação", "Localização", null,
                "Metodologia", DateRange.of(START, END), BUDGET, PaaLevel.UNIT_LEVEL);
        return draft.submit().approve().approve();
    }

    private ChangeRequest pendingRequest(TacticalActivityId activityId, String fieldName,
                                         String currentValue, String proposedValue) {
        return ChangeRequest.reconstruct(ChangeRequestId.gerarNovo(), INSTITUTION_ID, activityId,
                fieldName, currentValue, proposedValue,
                "Justificação com mais de cinquenta carateres para satisfazer a regra do ecrã.",
                ChangeRequestStatus.PENDING, null, null);
    }

    private ApproveChangeRequestCommand commandFor(ChangeRequest cr) {
        ApproveChangeRequestCommand command = new ApproveChangeRequestCommand();
        command.setId(cr.getId().getValor().getValor().toString());
        return command;
    }

    private TacticalActivity approveAndCaptureActivity(ChangeRequest cr, TacticalActivity activity) {
        when(changeRequestRepository.findById(any())).thenReturn(Optional.of(cr));
        when(activityRepository.findById(any())).thenReturn(Optional.of(activity));
        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<ChangeRequestResponseDTO> response = handler.handle(commandFor(cr));
        assertEquals(ChangeRequestStatus.APPROVED.getCode(), response.getBody().getStatus());

        ArgumentCaptor<TacticalActivity> captor = ArgumentCaptor.forClass(TacticalActivity.class);
        verify(activityRepository).save(captor.capture());
        return captor.getValue();
    }

    // Criterio 1 e 3: seguir a recomendacao da guarda do PAA-02 altera mesmo o orcamento, e a
    // atividade nao e despromovida a DRAFT -- volta a aprovacao tatica.
    @Test
    void handle_budgetChange_appliesNewAmountAndSendsActivityBackToTacticalApproval() {
        TacticalActivity activity = approvedActivity();
        ChangeRequest cr = pendingRequest(activity.getId(), "budget", "1000", "2500");

        TacticalActivity changed = approveAndCaptureActivity(cr, activity);

        assertEquals(0, new BigDecimal("2500").compareTo(changed.getBudget().getEstimatedAmount()));
        assertEquals(TacticalActivityStatus.PENDING_TACTICAL, changed.getStatus());
    }

    // O modal envia apenas o montante -- nao ha campo de classificador economico. O da atividade
    // tem de ser preservado, senao a alteracao de orcamento apagaria a classificacao orcamental.
    @Test
    void handle_budgetChange_keepsExistingEconomicClassifier() {
        TacticalActivity activity = approvedActivity();
        ChangeRequest cr = pendingRequest(activity.getId(), "budget", "1000", "2500");

        TacticalActivity changed = approveAndCaptureActivity(cr, activity);

        assertEquals(BUDGET.getClassifier(), changed.getBudget().getClassifier());
    }

    @Test
    void handle_titleChange_appliesTitleAndLeavesEverythingElseAlone() {
        TacticalActivity activity = approvedActivity();
        ChangeRequest cr = pendingRequest(activity.getId(), "title", "Atividade aprovada",
                "Atividade com título corrigido");

        TacticalActivity changed = approveAndCaptureActivity(cr, activity);

        assertEquals("Atividade com título corrigido", changed.getTitle());
        assertEquals(BUDGET, changed.getBudget());
        assertEquals(activity.getDateRange(), changed.getDateRange());
    }

    // Um pedido nomeia UM campo. Alterar a data de inicio nao pode arrastar a data de fim --
    // que e exatamente o que aconteceria se a aprovacao passasse pelo requestChange() em bloco.
    @Test
    void handle_startDateChange_movesOnlyThatEndOfTheRange() {
        TacticalActivity activity = approvedActivity();
        ChangeRequest cr = pendingRequest(activity.getId(), "start_date", "2026-01-01", "2026-03-01");

        TacticalActivity changed = approveAndCaptureActivity(cr, activity);

        assertEquals(LocalDate.of(2026, 3, 1), changed.getDateRange().getStartDate());
        assertEquals(END, changed.getDateRange().getEndDate());
    }

    @Test
    void handle_endDateChange_movesOnlyThatEndOfTheRange() {
        TacticalActivity activity = approvedActivity();
        ChangeRequest cr = pendingRequest(activity.getId(), "end_date", "2026-12-31", "2026-11-30");

        TacticalActivity changed = approveAndCaptureActivity(cr, activity);

        assertEquals(START, changed.getDateRange().getStartDate());
        assertEquals(LocalDate.of(2026, 11, 30), changed.getDateRange().getEndDate());
    }

    // Criterio 2, e a razao pela qual estes quatro testes existem: nao ha estado em que o pedido
    // diga APROVADO e a atividade fique por alterar. Em cada recusa, NENHUM dos dois e gravado.
    @Test
    void handle_unknownField_refusesAndSavesNothing() {
        TacticalActivity activity = approvedActivity();
        ChangeRequest cr = pendingRequest(activity.getId(), "orcamento", "1000", "2500");

        when(changeRequestRepository.findById(any())).thenReturn(Optional.of(cr));

        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(commandFor(cr)));

        verify(activityRepository, never()).save(any());
        verify(changeRequestRepository, never()).save(any());
    }

    @Test
    void handle_unparseableProposedValue_refusesAndSavesNothing() {
        TacticalActivity activity = approvedActivity();
        ChangeRequest cr = pendingRequest(activity.getId(), "budget", "1000", "dois mil e quinhentos");

        when(changeRequestRepository.findById(any())).thenReturn(Optional.of(cr));
        when(activityRepository.findById(any())).thenReturn(Optional.of(activity));

        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(commandFor(cr)));

        verify(activityRepository, never()).save(any());
        verify(changeRequestRepository, never()).save(any());
    }

    @Test
    void handle_missingActivity_refusesAndSavesNothing() {
        TacticalActivity activity = approvedActivity();
        ChangeRequest cr = pendingRequest(activity.getId(), "budget", "1000", "2500");

        when(changeRequestRepository.findById(any())).thenReturn(Optional.of(cr));
        when(activityRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(commandFor(cr)));

        verify(activityRepository, never()).save(any());
        verify(changeRequestRepository, never()).save(any());
    }

    @Test
    void handle_alreadyApprovedRequest_refusesAndSavesNothing() {
        TacticalActivity activity = approvedActivity();
        ChangeRequest cr = ChangeRequest.reconstruct(ChangeRequestId.gerarNovo(), INSTITUTION_ID,
                activity.getId(), "budget", "1000", "2500",
                "Justificação com mais de cinquenta carateres para satisfazer a regra do ecrã.",
                ChangeRequestStatus.APPROVED, null, "já aprovado antes");

        when(changeRequestRepository.findById(any())).thenReturn(Optional.of(cr));

        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(commandFor(cr)));

        verify(activityRepository, never()).save(any());
        verify(changeRequestRepository, never()).save(any());
    }
}

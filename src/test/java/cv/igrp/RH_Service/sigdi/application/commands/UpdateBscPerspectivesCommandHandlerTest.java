package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.BscPerspectiveItemDTO;
import cv.igrp.RH_Service.sigdi.application.dto.BscPerspectivesUpdateResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.IncoherentLinkDTO;
import cv.igrp.RH_Service.sigdi.application.service.StrategyLinkCoherencePolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UpdateBscPerspectivesCommandHandlerTest {

    @Mock
    private BscPerspectiveConfigRepository repository;

    // Wave 5 of Phase 130: the handler's second and last collaborator. It is the POLICY and not a
    // repository -- see the PERSP-03 section of the handler's javadoc. Mocking it here is also
    // what lets the "never consulted" assertions below be written at all.
    @Mock
    private StrategyLinkCoherencePolicy coherencePolicy;

    @InjectMocks
    private UpdateBscPerspectivesCommandHandler handler;

    private static BscPerspectiveConfig existing(String code, Integer order) {
        return BscPerspectiveConfig.reconstruct(UUID.randomUUID(), code, code + "-label", order);
    }

    private static List<BscPerspectiveItemDTO> validPermutation() {
        return List.of(
                new BscPerspectiveItemDTO("FINANCIAL", "Financeira", 1),
                new BscPerspectiveItemDTO("CUSTOMER", "Cliente / Mercado", 2),
                new BscPerspectiveItemDTO("PROCESS", "Processos Internos", 3),
                new BscPerspectiveItemDTO("LEARNING", "Aprendizagem e Crescimento", 4));
    }

    private void stubAllFourRows() {
        when(repository.findByCode("FINANCIAL")).thenReturn(Optional.of(existing("FINANCIAL", 1)));
        when(repository.findByCode("CUSTOMER")).thenReturn(Optional.of(existing("CUSTOMER", 2)));
        when(repository.findByCode("PROCESS")).thenReturn(Optional.of(existing("PROCESS", 3)));
        when(repository.findByCode("LEARNING")).thenReturn(Optional.of(existing("LEARNING", 4)));
    }

    private static IncoherentLinkDTO anIncoherentLink() {
        IncoherentLinkDTO link = new IncoherentLinkDTO();
        link.setLinkId(UUID.randomUUID());
        link.setSourceGoalId(UUID.randomUUID());
        link.setSourceGoalTitle("Formar as equipas");
        link.setSourcePerspective("LEARNING");
        link.setSourcePerspectiveOrder(3);
        link.setTargetGoalId(UUID.randomUUID());
        link.setTargetGoalTitle("Digitalizar o atendimento");
        link.setTargetPerspective("PROCESS");
        link.setTargetPerspectiveOrder(4);
        link.setReason(IncoherentLinkDTO.Reason.CAUSE_EFFECT_ORDER_INVERTED);
        link.setReasons(List.of(IncoherentLinkDTO.Reason.CAUSE_EFFECT_ORDER_INVERTED));
        link.setReasonDesc("A perspetiva de origem está numa ordem inferior à da perspetiva de destino"
                + " no fluxo causa-efeito do BSC.");
        return link;
    }

    /**
     * CORRECTED BY WAVE 5 OF PHASE 130, AND THE REASON IS THE RESPONSE SHAPE AND NOTHING ELSE.
     * The endpoint used to answer a bare List&lt;BscPerspectiveItemDTO&gt; and now answers a
     * BscPerspectivesUpdateResponseDTO envelope (FIX-10 / A-126-05). What this test asserts is
     * unchanged -- four rows updated, saveAll called once with four items -- and it now reads them
     * through response.getBody().getPerspectives(). The policy stub is required because the
     * handler consults it after the save; the coherence assertions are the new tests below.
     */
    @Test
    void handleWithValidPermutationUpdatesAllFourAndSavesOnce() {
        stubAllFourRows();
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(coherencePolicy.findIncoherentLinksIfIdentityActive()).thenReturn(Optional.of(List.of()));

        List<BscPerspectiveItemDTO> requested = validPermutation();

        ResponseEntity<BscPerspectivesUpdateResponseDTO> response =
                handler.handle(new UpdateBscPerspectivesCommand(requested));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(4, response.getBody().getPerspectives().size());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BscPerspectiveConfig>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertEquals(4, captor.getValue().size());
    }

    /**
     * REVIEWED BY WAVE 5 AND LEFT AS IT WAS, WHICH IS ALSO A DECISION AND SO IS WRITTEN DOWN. The
     * response type changed, but this test never reads a response: it asserts the call throws. It
     * needed no edit, and editing it to look busy would have been a change with no reason behind
     * it. The one line added is the assertion that the policy is not consulted on a refusal --
     * that is new behaviour to fix, not old behaviour to preserve.
     */
    @Test
    void handleWithGappedOrderThrowsBadRequestAndNeverSaves() {
        // orders {1,2,3,3}: PROCESS and LEARNING both claim position 3, no item claims 4.
        List<BscPerspectiveItemDTO> requested = List.of(
                new BscPerspectiveItemDTO("FINANCIAL", "Financeira", 1),
                new BscPerspectiveItemDTO("CUSTOMER", "Cliente / Mercado", 2),
                new BscPerspectiveItemDTO("PROCESS", "Processos Internos", 3),
                new BscPerspectiveItemDTO("LEARNING", "Aprendizagem e Crescimento", 3));

        assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new UpdateBscPerspectivesCommand(requested)));

        verify(repository, never()).findByCode(any());
        verify(repository, never()).saveAll(any());
        verify(coherencePolicy, never()).findIncoherentLinksIfIdentityActive();
    }

    /**
     * REVIEWED BY WAVE 5 AND LEFT AS IT WAS, for the same reason as the test above: it asserts a
     * throw and never reads the response, so the new envelope does not reach it. The added line is
     * the policy-never-consulted assertion.
     */
    @Test
    void handleWithCodeMissingFromRepositoryThrowsBadRequestAndNeverSaves() {
        // All 4 codes are the correct, expected codes (passes the code-set check), but the
        // repository unexpectedly has no row for LEARNING -- exercises the defensive
        // findByCode(...).orElseThrow(...) branch (T-73-06).
        when(repository.findByCode("FINANCIAL")).thenReturn(Optional.of(existing("FINANCIAL", 1)));
        when(repository.findByCode("CUSTOMER")).thenReturn(Optional.of(existing("CUSTOMER", 2)));
        when(repository.findByCode("PROCESS")).thenReturn(Optional.of(existing("PROCESS", 3)));
        when(repository.findByCode("LEARNING")).thenReturn(Optional.empty());

        List<BscPerspectiveItemDTO> requested = List.of(
                new BscPerspectiveItemDTO("FINANCIAL", "Financeira", 1),
                new BscPerspectiveItemDTO("CUSTOMER", "Cliente / Mercado", 2),
                new BscPerspectiveItemDTO("PROCESS", "Processos Internos", 3),
                new BscPerspectiveItemDTO("LEARNING", "Aprendizagem e Crescimento", 4));

        assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new UpdateBscPerspectivesCommand(requested)));

        verify(repository, never()).saveAll(any());
        verify(coherencePolicy, never()).findIncoherentLinksIfIdentityActive();
    }

    /**
     * REVIEWED BY WAVE 5 AND LEFT AS IT WAS, same reason again -- an assertThrows never touches the
     * response body. The added line is the policy-never-consulted assertion.
     */
    @Test
    void handleWithDuplicateValidCodeThrowsBadRequestBeforeAnyLookupOrSave() {
        // Two FINANCIAL items, no LEARNING -- order values {1,2,3,4} would otherwise be a valid
        // permutation, so this specifically proves the code-set check runs BEFORE the order check.
        List<BscPerspectiveItemDTO> requested = List.of(
                new BscPerspectiveItemDTO("FINANCIAL", "Financeira", 1),
                new BscPerspectiveItemDTO("FINANCIAL", "Financeira Duplicada", 2),
                new BscPerspectiveItemDTO("PROCESS", "Processos Internos", 3),
                new BscPerspectiveItemDTO("CUSTOMER", "Cliente / Mercado", 4));

        assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new UpdateBscPerspectivesCommand(requested)));

        verify(repository, never()).findByCode(any());
        verify(repository, never()).saveAll(any());
        verify(coherencePolicy, never()).findIncoherentLinksIfIdentityActive();
    }

    // ---------------------------------------------------------------------------------------
    // Wave 5 of Phase 130 -- FIX-10 / A-126-05. The PERSP-03 crossing: read and warn, never
    // write and never refuse.
    // ---------------------------------------------------------------------------------------

    /**
     * The quiet half of the rule: a reordering that breaks nothing warns about nothing, and the
     * save still happens. Without this case, a handler that reported every link as incoherent
     * would look correct.
     */
    @Test
    @DisplayName("A valid permutation that breaks nothing saves and returns an EVALUATED, empty warning list")
    void validPermutationWithNothingIncoherentSavesAndWarnsAboutNothing() {
        stubAllFourRows();
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(coherencePolicy.findIncoherentLinksIfIdentityActive()).thenReturn(Optional.of(List.of()));

        ResponseEntity<BscPerspectivesUpdateResponseDTO> response =
                handler.handle(new UpdateBscPerspectivesCommand(validPermutation()));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(BscPerspectivesUpdateResponseDTO.CoherenceCheck.EVALUATED,
                response.getBody().getCoherenceCheck());
        assertTrue(response.getBody().getIncoherentLinks().isEmpty());
        verify(repository).saveAll(any());
    }

    /**
     * THE WHOLE POINT OF THE WAVE, IN ONE TEST: the reordering makes a stored link incoherent, the
     * response names it, AND THE SAVE STILL HAPPENS. Decision 2 of 130-CONTEXT.md is warn and save.
     * If a future change turned this into a refusal, the saveAll verification below is what would
     * fail -- and it would fail loudly, rather than the endpoint quietly becoming stricter.
     */
    @Test
    @DisplayName("A permutation that makes a stored link incoherent still SAVES, and returns the link")
    void permutationThatBreaksAStoredLinkStillSavesAndReturnsTheWarning() {
        stubAllFourRows();
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(coherencePolicy.findIncoherentLinksIfIdentityActive())
                .thenReturn(Optional.of(List.of(anIncoherentLink())));

        ResponseEntity<BscPerspectivesUpdateResponseDTO> response =
                handler.handle(new UpdateBscPerspectivesCommand(validPermutation()));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(4, response.getBody().getPerspectives().size());
        assertEquals(1, response.getBody().getIncoherentLinks().size());
        assertEquals(IncoherentLinkDTO.Reason.CAUSE_EFFECT_ORDER_INVERTED,
                response.getBody().getIncoherentLinks().get(0).getReason());

        // It WARNED. It did not refuse: the four rows were written.
        verify(repository).saveAll(any());
    }

    /**
     * The absence that has to stay distinguishable: no active institutional identity means the
     * links could not be read at all, which is NOT "there is nothing to warn about". Rule 5 of the
     * project CLAUDE.md, and its corollary -- an absence may only be presented as legitimate when
     * its cause was discriminated instead of presumed.
     */
    @Test
    @DisplayName("With no active identity the save happens and the envelope says the check did not run")
    void withNoActiveIdentityTheSaveHappensAndTheCheckIsReportedAsNotRun() {
        stubAllFourRows();
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(coherencePolicy.findIncoherentLinksIfIdentityActive()).thenReturn(Optional.empty());

        ResponseEntity<BscPerspectivesUpdateResponseDTO> response =
                handler.handle(new UpdateBscPerspectivesCommand(validPermutation()));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(BscPerspectivesUpdateResponseDTO.CoherenceCheck.NOT_EVALUATED_NO_ACTIVE_IDENTITY,
                response.getBody().getCoherenceCheck());
        // Null, and deliberately not an empty list: an empty list is an answer, and there is none.
        assertNull(response.getBody().getIncoherentLinks());
        verify(repository).saveAll(any());
    }
}

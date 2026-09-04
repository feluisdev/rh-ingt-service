package cv.igrp.RH_Service.sigdi.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.commands.CreateStrategyMapLinkCommand;
import cv.igrp.RH_Service.sigdi.application.commands.CreateStrategyMapLinkCommandHandler;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.application.constants.StrategyMapRelationshipType;
import cv.igrp.RH_Service.sigdi.application.dto.IncoherentLinkDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyLinkDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategyMapLink;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategyMapLinkRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalValues;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * THE MIRROR TEST, AND IT IS NOT OPTIONAL.
 *
 * <p>{@link StrategyLinkCoherencePolicy} is the single place where "would the rule in force still
 * allow this link to be created?" is written, but it is NOT a refactoring of
 * {@link CreateStrategyMapLinkCommandHandler}: that handler keeps throwing its own exceptions in
 * its own places, deliberately, because refactoring it would have been new risk in a wave that
 * already changes the {@code StrategicGoal} aggregate. Nothing in the production code therefore
 * forces the two to agree.
 *
 * <p><b>This test is what keeps them saying the same thing.</b> For the same pairs of
 * perspectives it runs the real handler and the real policy side by side and asserts that the
 * policy marks incoherent EXACTLY the pairs the handler refuses. If the two ever drift, the
 * product would warn about one rule and refuse by another, which is worse than either rule alone.
 *
 * <p><b>Wave 5 of Phase 130 EXTENDED this test, and did not replace it.</b> The {@code status}
 * clause has landed -- linking to a cancelled goal is refused, decision 3 of
 * {@code 130-CONTEXT.md} -- and the pair table below grew a cancelled-goal row on BOTH sides plus
 * a row that breaks the two clauses at once. Without those rows the two implementations of that
 * clause would have been free to disagree from the day it was written.
 *
 * <p>Mocks are built by hand rather than through {@code MockitoExtension} on purpose: each case
 * drives TWO collaborators with different call shapes (the handler asks {@code findByCode}, the
 * policy asks {@code findAll}), and strict-stub accounting across both would fail cases where one
 * side short-circuits before reaching a stub -- for reasons that have nothing to do with the rule
 * under test.
 */
class StrategyLinkCoherencePolicyTest {

    /** The V26-seeded display order, which is configuration and not any enum's ordinal(). */
    private static final Map<String, Integer> SEEDED_ORDERS = new LinkedHashMap<>(Map.of(
            "FINANCIAL", 1, "CUSTOMER", 2, "PROCESS", 3, "LEARNING", 4));

    /** The same configuration with LEARNING missing -- the "perspetiva não configurada" case. */
    private static final Map<String, Integer> ORDERS_WITHOUT_LEARNING = new LinkedHashMap<>(Map.of(
            "FINANCIAL", 1, "CUSTOMER", 2, "PROCESS", 3));

    private InstitutionalIdentity activeIdentity() {
        return InstitutionalIdentity.create(UUID.randomUUID(), 2026, "Missão de teste",
                "Visão de teste", InstitutionalValues.of(List.of("Integridade")), "comentário de teste");
    }

    private StrategicGoal goalWithPerspective(InstitutionalIdentityId identityId, String title,
            StrategicGoalsPerspective perspective) {
        return StrategicGoal.reconstruct(StrategicGoalId.gerarNovo(), UUID.randomUUID(), identityId,
                title, perspective, BigDecimal.ONE, Estado.A, "Descrição de teste",
                null, null, 2026, List.of());
    }

    /**
     * The same goal, cancelled. Estado.I is the persisted "Inativo" code, and it is read through
     * {@code StrategicGoal.isActive()} on both sides -- never by comparing strings.
     */
    private StrategicGoal cancelledGoalWithPerspective(InstitutionalIdentityId identityId, String title,
            StrategicGoalsPerspective perspective) {
        return StrategicGoal.reconstruct(StrategicGoalId.gerarNovo(), UUID.randomUUID(), identityId,
                title, perspective, BigDecimal.ONE, Estado.I, "Descrição de teste",
                null, null, 2026, List.of());
    }

    private List<BscPerspectiveConfig> configsFrom(Map<String, Integer> orders) {
        List<BscPerspectiveConfig> configs = new ArrayList<>();
        orders.forEach((code, order) ->
                configs.add(BscPerspectiveConfig.reconstruct(UUID.randomUUID(), code, code, order)));
        return configs;
    }

    /** Runs the REAL CreateStrategyMapLinkCommandHandler and reports whether it refused. */
    private boolean createHandlerRefuses(InstitutionalIdentity identity, StrategicGoal source,
            StrategicGoal target, Map<String, Integer> orders) {
        InstitutionalIdentityRepository identityRepository = mock(InstitutionalIdentityRepository.class);
        StrategicGoalRepository goalRepository = mock(StrategicGoalRepository.class);
        StrategyMapLinkRepository linkRepository = mock(StrategyMapLinkRepository.class);
        BscPerspectiveConfigRepository configRepository = mock(BscPerspectiveConfigRepository.class);

        when(identityRepository.findActive()).thenReturn(Optional.of(identity));
        when(goalRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(goalRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(configRepository.findByCode(anyString())).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            Integer order = orders.get(code);
            return order == null
                    ? Optional.empty()
                    : Optional.of(BscPerspectiveConfig.reconstruct(UUID.randomUUID(), code, code, order));
        });
        when(linkRepository.findBySourceAndTarget(any(StrategicGoalId.class), any(StrategicGoalId.class)))
                .thenReturn(Optional.empty());
        when(linkRepository.save(any(StrategyMapLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateStrategyMapLinkCommandHandler handler = new CreateStrategyMapLinkCommandHandler(
                identityRepository, goalRepository, linkRepository, configRepository);

        StrategyLinkDTO dto = new StrategyLinkDTO(source.getId().getStringValor(),
                target.getId().getStringValor(), StrategyMapRelationshipType.CAUSE_EFFECT.getCode());
        try {
            handler.handle(new CreateStrategyMapLinkCommand(dto));
            return false;
        } catch (IgrpResponseStatusException refused) {
            return true;
        }
    }

    /** Runs the REAL StrategyLinkCoherencePolicy over one stored link and returns what it reported. */
    private List<IncoherentLinkDTO> policyReportFor(InstitutionalIdentity identity, StrategicGoal source,
            StrategicGoal target, Map<String, Integer> orders) {
        InstitutionalIdentityRepository identityRepository = mock(InstitutionalIdentityRepository.class);
        StrategicGoalRepository goalRepository = mock(StrategicGoalRepository.class);
        StrategyMapLinkRepository linkRepository = mock(StrategyMapLinkRepository.class);
        BscPerspectiveConfigRepository configRepository = mock(BscPerspectiveConfigRepository.class);

        StrategyMapLink storedLink = StrategyMapLink.create(identity.getInstitutionId(),
                source.getId(), target.getId(), StrategyMapRelationshipType.CAUSE_EFFECT);

        when(identityRepository.findActive()).thenReturn(Optional.of(identity));
        when(linkRepository.findByIdentityId(identity.getId())).thenReturn(List.of(storedLink));
        when(goalRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(goalRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(configRepository.findAll()).thenReturn(configsFrom(orders));

        StrategyLinkCoherencePolicy policy = new StrategyLinkCoherencePolicy(
                identityRepository, linkRepository, goalRepository, configRepository);

        return policy.findIncoherentLinks();
    }

    /**
     * The mirror, over the seven cases the two clauses admit: source above the target, source
     * below it, both at the same order, a perspective with no configured row at all, a cancelled
     * source, a cancelled target, and a pair that breaks the status clause AND the order clause at
     * once.
     *
     * <p>The three cancelled rows are wave 5's addition. Note that the cancelled rows use
     * LEARNING(4) -> PROCESS(3) -- the direction the rule ALLOWS, and the exact pair the first row
     * proves is coherent when both goals are active. That is deliberate: it makes the status the
     * only variable, so a row cannot pass for the wrong reason.
     */
    @Test
    @DisplayName("The policy marks incoherent exactly the pairs CreateStrategyMapLinkCommandHandler refuses")
    void policyMarksExactlyWhatTheCreateHandlerRefuses() {
        record Pair(String name, StrategicGoalsPerspective source, StrategicGoalsPerspective target,
                Map<String, Integer> orders, boolean sourceCancelled, boolean targetCancelled) {
        }

        List<Pair> pairs = List.of(
                new Pair("origem acima do destino (LEARNING 4 -> PROCESS 3)",
                        StrategicGoalsPerspective.LEARNING, StrategicGoalsPerspective.PROCESS, SEEDED_ORDERS,
                        false, false),
                new Pair("origem abaixo do destino (FINANCIAL 1 -> LEARNING 4)",
                        StrategicGoalsPerspective.FINANCIAL, StrategicGoalsPerspective.LEARNING, SEEDED_ORDERS,
                        false, false),
                new Pair("mesma ordem (PROCESS 3 -> PROCESS 3)",
                        StrategicGoalsPerspective.PROCESS, StrategicGoalsPerspective.PROCESS, SEEDED_ORDERS,
                        false, false),
                new Pair("perspetiva não configurada (LEARNING sem linha -> PROCESS 3)",
                        StrategicGoalsPerspective.LEARNING, StrategicGoalsPerspective.PROCESS,
                        ORDERS_WITHOUT_LEARNING, false, false),
                new Pair("origem cancelada, direção permitida (LEARNING 4 -> PROCESS 3)",
                        StrategicGoalsPerspective.LEARNING, StrategicGoalsPerspective.PROCESS, SEEDED_ORDERS,
                        true, false),
                new Pair("destino cancelado, direção permitida (LEARNING 4 -> PROCESS 3)",
                        StrategicGoalsPerspective.LEARNING, StrategicGoalsPerspective.PROCESS, SEEDED_ORDERS,
                        false, true),
                new Pair("as duas cláusulas violadas (FINANCIAL 1 -> LEARNING 4, os dois cancelados)",
                        StrategicGoalsPerspective.FINANCIAL, StrategicGoalsPerspective.LEARNING, SEEDED_ORDERS,
                        true, true));

        for (Pair pair : pairs) {
            InstitutionalIdentity identity = activeIdentity();
            StrategicGoal source = pair.sourceCancelled()
                    ? cancelledGoalWithPerspective(identity.getId(), "Objetivo de origem", pair.source())
                    : goalWithPerspective(identity.getId(), "Objetivo de origem", pair.source());
            StrategicGoal target = pair.targetCancelled()
                    ? cancelledGoalWithPerspective(identity.getId(), "Objetivo de destino", pair.target())
                    : goalWithPerspective(identity.getId(), "Objetivo de destino", pair.target());

            boolean handlerRefuses = createHandlerRefuses(identity, source, target, pair.orders());
            boolean policyMarks = !policyReportFor(identity, source, target, pair.orders()).isEmpty();

            assertEquals(handlerRefuses, policyMarks,
                    "Divergência entre o sítio que recusa e o sítio que avisa, no caso: " + pair.name()
                            + ". CreateStrategyMapLinkCommandHandler recusa=" + handlerRefuses
                            + ", StrategyLinkCoherencePolicy marca=" + policyMarks
                            + ". Os dois têm de dizer o mesmo para o mesmo par.");
        }
    }

    @Test
    @DisplayName("Source above target is coherent -- nothing is reported")
    void sourceAboveTargetIsCoherentAndIsNotReported() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal source = goalWithPerspective(identity.getId(), "Formar as equipas",
                StrategicGoalsPerspective.LEARNING);
        StrategicGoal target = goalWithPerspective(identity.getId(), "Digitalizar o atendimento",
                StrategicGoalsPerspective.PROCESS);

        assertTrue(policyReportFor(identity, source, target, SEEDED_ORDERS).isEmpty());
    }

    @Test
    @DisplayName("Source below target is reported with the inverted-order reason")
    void sourceBelowTargetIsReportedWithTheInvertedOrderReason() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal source = goalWithPerspective(identity.getId(), "Reduzir o custo unitário",
                StrategicGoalsPerspective.FINANCIAL);
        StrategicGoal target = goalWithPerspective(identity.getId(), "Formar as equipas",
                StrategicGoalsPerspective.LEARNING);

        List<IncoherentLinkDTO> reported = policyReportFor(identity, source, target, SEEDED_ORDERS);

        assertEquals(1, reported.size());
        assertEquals(IncoherentLinkDTO.Reason.CAUSE_EFFECT_ORDER_INVERTED, reported.get(0).getReason());
        assertEquals("Reduzir o custo unitário", reported.get(0).getSourceGoalTitle());
        assertEquals("Formar as equipas", reported.get(0).getTargetGoalTitle());
        assertEquals(Integer.valueOf(1), reported.get(0).getSourcePerspectiveOrder());
        assertEquals(Integer.valueOf(4), reported.get(0).getTargetPerspectiveOrder());
    }

    @Test
    @DisplayName("Two goals at the same order are coherent -- the rule refuses only a LOWER source")
    void sameOrderIsCoherentAndIsNotReported() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal source = goalWithPerspective(identity.getId(), "Objetivo A",
                StrategicGoalsPerspective.PROCESS);
        StrategicGoal target = goalWithPerspective(identity.getId(), "Objetivo B",
                StrategicGoalsPerspective.PROCESS);

        assertTrue(policyReportFor(identity, source, target, SEEDED_ORDERS).isEmpty());
    }

    /**
     * The fourth case, and it carries its OWN reason. Folding it into
     * {@code CAUSE_EFFECT_ORDER_INVERTED} would produce a warning the reader cannot act on: "the
     * direction is wrong" is fixed by re-pointing the link, "the order cannot be read" is fixed by
     * configuring the perspective. Rule 5 of the project CLAUDE.md forbids collapsing the two.
     */
    @Test
    @DisplayName("An unconfigured perspective is reported with its own reason, not folded into the order one")
    void unconfiguredPerspectiveIsReportedWithItsOwnReason() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal source = goalWithPerspective(identity.getId(), "Formar as equipas",
                StrategicGoalsPerspective.LEARNING);
        StrategicGoal target = goalWithPerspective(identity.getId(), "Digitalizar o atendimento",
                StrategicGoalsPerspective.PROCESS);

        List<IncoherentLinkDTO> reported = policyReportFor(identity, source, target, ORDERS_WITHOUT_LEARNING);

        assertEquals(1, reported.size());
        assertEquals(IncoherentLinkDTO.Reason.PERSPECTIVE_NOT_CONFIGURED, reported.get(0).getReason());
    }

    /**
     * Wave 5's clause, reported with its own reason and with the END named. A stored link to a
     * cancelled goal is REPORTED here and REFUSED in the create handler -- the asymmetry of
     * decision 3 of {@code 130-CONTEXT.md} is about creating a NEW incoherence versus naming one
     * that is already stored, and this test is the second half of it.
     */
    @Test
    @DisplayName("A cancelled goal is reported with its own reason, and the description names which end")
    void cancelledGoalIsReportedWithItsOwnReason() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal activeSource = goalWithPerspective(identity.getId(), "Formar as equipas",
                StrategicGoalsPerspective.LEARNING);
        StrategicGoal cancelledTarget = cancelledGoalWithPerspective(identity.getId(),
                "Digitalizar o atendimento", StrategicGoalsPerspective.PROCESS);

        List<IncoherentLinkDTO> reported =
                policyReportFor(identity, activeSource, cancelledTarget, SEEDED_ORDERS);

        assertEquals(1, reported.size());
        assertEquals(IncoherentLinkDTO.Reason.GOAL_CANCELLED, reported.get(0).getReason());
        assertEquals(List.of(IncoherentLinkDTO.Reason.GOAL_CANCELLED), reported.get(0).getReasons());
        assertEquals("O objetivo de destino está cancelado.", reported.get(0).getReasonDesc());

        StrategicGoal cancelledSource = cancelledGoalWithPerspective(identity.getId(),
                "Formar as equipas", StrategicGoalsPerspective.LEARNING);
        StrategicGoal activeTarget = goalWithPerspective(identity.getId(), "Digitalizar o atendimento",
                StrategicGoalsPerspective.PROCESS);

        List<IncoherentLinkDTO> otherEnd =
                policyReportFor(identity, cancelledSource, activeTarget, SEEDED_ORDERS);

        assertEquals(1, otherEnd.size());
        assertEquals(IncoherentLinkDTO.Reason.GOAL_CANCELLED, otherEnd.get(0).getReason());
        assertEquals("O objetivo de origem está cancelado.", otherEnd.get(0).getReasonDesc());
    }

    /**
     * A warning that hides half of its own cause is a warning by halves: the reader would fix the
     * status, come back, and meet the same link reported again for the direction. Both reasons
     * travel, and {@code reasonDesc} carries both sentences -- which is what the two consumers
     * render, neither of them keeping a translation table of its own.
     */
    @Test
    @DisplayName("A link that breaks both clauses is reported with BOTH reasons, not just the first")
    void aLinkBreakingBothClausesReportsBothReasons() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal source = cancelledGoalWithPerspective(identity.getId(), "Reduzir o custo unitário",
                StrategicGoalsPerspective.FINANCIAL);
        StrategicGoal target = cancelledGoalWithPerspective(identity.getId(), "Formar as equipas",
                StrategicGoalsPerspective.LEARNING);

        List<IncoherentLinkDTO> reported = policyReportFor(identity, source, target, SEEDED_ORDERS);

        assertEquals(1, reported.size());
        assertEquals(List.of(IncoherentLinkDTO.Reason.GOAL_CANCELLED,
                IncoherentLinkDTO.Reason.CAUSE_EFFECT_ORDER_INVERTED), reported.get(0).getReasons());
        // The single-valued field keeps the FIRST cause, in the order the create handler evaluates
        // its guards -- status before perspective.
        assertEquals(IncoherentLinkDTO.Reason.GOAL_CANCELLED, reported.get(0).getReason());
        assertTrue(reported.get(0).getReasonDesc().contains("Os dois objetivos ligados estão cancelados."));
        assertTrue(reported.get(0).getReasonDesc().contains("ordem inferior"));
    }

    @Test
    @DisplayName("findIncoherentLinksForGoal restricts to the named goal and returns empty for a goal with no links")
    void findIncoherentLinksForGoalRestrictsToTheNamedGoal() {
        InstitutionalIdentityRepository identityRepository = mock(InstitutionalIdentityRepository.class);
        StrategicGoalRepository goalRepository = mock(StrategicGoalRepository.class);
        StrategyMapLinkRepository linkRepository = mock(StrategyMapLinkRepository.class);
        BscPerspectiveConfigRepository configRepository = mock(BscPerspectiveConfigRepository.class);

        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal source = goalWithPerspective(identity.getId(), "Reduzir o custo unitário",
                StrategicGoalsPerspective.FINANCIAL);
        StrategicGoal target = goalWithPerspective(identity.getId(), "Formar as equipas",
                StrategicGoalsPerspective.LEARNING);
        StrategicGoal unrelated = goalWithPerspective(identity.getId(), "Objetivo sem ligações",
                StrategicGoalsPerspective.CUSTOMER);

        StrategyMapLink storedLink = StrategyMapLink.create(identity.getInstitutionId(),
                source.getId(), target.getId(), StrategyMapRelationshipType.CAUSE_EFFECT);

        when(identityRepository.findActive()).thenReturn(Optional.of(identity));
        when(linkRepository.findByIdentityId(identity.getId())).thenReturn(List.of(storedLink));
        when(goalRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(goalRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(configRepository.findAll()).thenReturn(configsFrom(SEEDED_ORDERS));

        StrategyLinkCoherencePolicy policy = new StrategyLinkCoherencePolicy(
                identityRepository, linkRepository, goalRepository, configRepository);

        assertEquals(1, policy.findIncoherentLinksForGoal(source.getId()).size());
        assertEquals(1, policy.findIncoherentLinksForGoal(target.getId()).size());

        // The point that must not be discovered while reading a result: a goal with no links at all
        // yields an empty list BY CONSTRUCTION. Empty here says "nothing to warn about for THIS
        // goal", and says nothing at all about links between other goals.
        assertTrue(policy.findIncoherentLinksForGoal(unrelated.getId()).isEmpty());
    }

    @Test
    @DisplayName("No active identity yields an empty list and never an exception")
    void noActiveIdentityYieldsAnEmptyListAndNeverAnException() {
        InstitutionalIdentityRepository identityRepository = mock(InstitutionalIdentityRepository.class);
        StrategicGoalRepository goalRepository = mock(StrategicGoalRepository.class);
        StrategyMapLinkRepository linkRepository = mock(StrategyMapLinkRepository.class);
        BscPerspectiveConfigRepository configRepository = mock(BscPerspectiveConfigRepository.class);

        when(identityRepository.findActive()).thenReturn(Optional.empty());

        StrategyLinkCoherencePolicy policy = new StrategyLinkCoherencePolicy(
                identityRepository, linkRepository, goalRepository, configRepository);

        assertTrue(policy.findIncoherentLinks().isEmpty());
        assertTrue(policy.findIncoherentLinksForGoal(StrategicGoalId.gerarNovo()).isEmpty());

        // And the distinction wave 5 needs: an empty list from findIncoherentLinks() is
        // indistinguishable from "nothing was evaluated". findIncoherentLinksIfIdentityActive()
        // answers Optional.empty() for the second, which is what lets the BSC-perspective response
        // tell the user WHY the list is empty instead of implying there is nothing to warn about.
        assertTrue(policy.findIncoherentLinksIfIdentityActive().isEmpty());
    }

    /**
     * The policy is READ-ONLY, and that is a contract of decision 2 of {@code 130-CONTEXT.md}:
     * it warns, it never corrects and it never refuses. Asserted here as well as by inspection of
     * the source, because an inspection cannot be run by a build.
     */
    @Test
    @DisplayName("The policy never writes to any of its four repository ports")
    void policyNeverWritesToAnyRepositoryPort() {
        InstitutionalIdentityRepository identityRepository = mock(InstitutionalIdentityRepository.class);
        StrategicGoalRepository goalRepository = mock(StrategicGoalRepository.class);
        StrategyMapLinkRepository linkRepository = mock(StrategyMapLinkRepository.class);
        BscPerspectiveConfigRepository configRepository = mock(BscPerspectiveConfigRepository.class);

        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal source = goalWithPerspective(identity.getId(), "Reduzir o custo unitário",
                StrategicGoalsPerspective.FINANCIAL);
        StrategicGoal target = goalWithPerspective(identity.getId(), "Formar as equipas",
                StrategicGoalsPerspective.LEARNING);
        StrategyMapLink storedLink = StrategyMapLink.create(identity.getInstitutionId(),
                source.getId(), target.getId(), StrategyMapRelationshipType.CAUSE_EFFECT);

        when(identityRepository.findActive()).thenReturn(Optional.of(identity));
        when(linkRepository.findByIdentityId(identity.getId())).thenReturn(List.of(storedLink));
        when(goalRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(goalRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(configRepository.findAll()).thenReturn(configsFrom(SEEDED_ORDERS));

        StrategyLinkCoherencePolicy policy = new StrategyLinkCoherencePolicy(
                identityRepository, linkRepository, goalRepository, configRepository);

        policy.findIncoherentLinks();
        policy.findIncoherentLinksForGoal(source.getId());

        verify(goalRepository, never()).save(any(StrategicGoal.class));
        verify(linkRepository, never()).save(any(StrategyMapLink.class));
        verify(linkRepository, never()).delete(any());
        verify(identityRepository, never()).save(any(InstitutionalIdentity.class));
        verify(configRepository, never()).saveAll(any());
    }
}

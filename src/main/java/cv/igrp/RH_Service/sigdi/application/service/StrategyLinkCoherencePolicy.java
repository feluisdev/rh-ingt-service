package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.sigdi.application.dto.IncoherentLinkDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategyMapLink;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategyMapLinkRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The single place where "would the rule in force still allow this stored link to be created?" is
 * written.
 *
 * <p><b>Why it exists at all.</b> That question is needed in THREE places: the link handler, which
 * REFUSES ({@code CreateStrategyMapLinkCommandHandler}); updating a strategic goal, which WARNS
 * (FIX-09 / A-124-02, wave 4 of Phase 130); and updating the BSC perspective order, which also
 * WARNS (FIX-10 / A-126-05, wave 5). Three copies would drift, and drifting here means the product
 * warning about one rule and refusing by another.
 *
 * <p><b>THIS CLASS IS READ-ONLY, and that is a contract and not an accident.</b> It never calls
 * {@code save}, never calls {@code delete}, and never throws a refusal. It returns lists. Decision
 * 2 of {@code 130-CONTEXT.md} is "warn and save", and refusing would put the user in a deadlock
 * the product explains nowhere. The asymmetry with decision 3 -- creating a NEW incoherence is
 * refused -- is deliberate: an incoherence a legitimate configuration produced after the fact is
 * warned about, not refused.
 *
 * <p><b>The rule below is the MIRROR of BOTH clauses of
 * {@code CreateStrategyMapLinkCommandHandler}, and not of one.</b> Wave 5 of Phase 130 added the
 * second: (a) the STATUS clause -- neither end may be a cancelled goal (FIX-10 / A-125-01), which
 * the handler evaluates first, right after loading the two goals; and (b) the PERSPECTIVE clause
 * -- both perspectives must be configured and the source's display order must not be lower than
 * the target's. The two must say the same thing for the same pair. What keeps them together is NOT shared control
 * flow -- the handler keeps throwing its own exceptions in its own places, because refactoring it
 * would be new risk in a wave that already changes the aggregate -- it is a test:
 * {@code StrategyLinkCoherencePolicyTest#policyMarksExactlyWhatTheCreateHandlerRefuses}. If that
 * test is deleted, nothing else stops the two from drifting.
 *
 * <p><b>The order is configurable and is not any enum's {@code ordinal()}.</b> It is read from
 * {@link BscPerspectiveConfigRepository}, which is exactly why reordering perspectives can make a
 * stored link incoherent without anyone touching the link.
 *
 * <p><b>Why it lives in {@code application.service}.</b> Same reason as
 * {@link SelfEvaluationWindowPolicy}: the question crosses aggregates -- it reads links, goals and
 * perspective configuration to answer about a single link -- and it depends on repositories to do
 * so, so it cannot take the static, dependency-free shape of {@code SiadapQuotaPolicy}.
 */
@Component
public class StrategyLinkCoherencePolicy {

  private final InstitutionalIdentityRepository identityRepository;
  private final StrategyMapLinkRepository linkRepository;
  private final StrategicGoalRepository goalRepository;
  private final BscPerspectiveConfigRepository perspectiveConfigRepository;

  public StrategyLinkCoherencePolicy(InstitutionalIdentityRepository identityRepository,
                                     StrategyMapLinkRepository linkRepository,
                                     StrategicGoalRepository goalRepository,
                                     BscPerspectiveConfigRepository perspectiveConfigRepository) {
    this.identityRepository = identityRepository;
    this.linkRepository = linkRepository;
    this.goalRepository = goalRepository;
    this.perspectiveConfigRepository = perspectiveConfigRepository;
  }

  /**
   * Every stored link of the active institutional identity that the rule in force would no longer
   * allow to be created. Returns an empty list -- never null, never an exception -- when there is
   * no active identity or no link at all.
   *
   * <p>This is the form wave 5 consumes after reordering perspectives: the change is global, so
   * the question is global too.
   */
  public List<IncoherentLinkDTO> findIncoherentLinks() {
    return findIncoherentLinksIfIdentityActive().orElse(List.of());
  }

  /**
   * The same global question as {@link #findIncoherentLinks()}, but WITH the one distinction that
   * method cannot make: {@code Optional.empty()} means "there is no active institutional identity,
   * so nothing was evaluated", and {@code Optional.of(emptyList)} means "everything was evaluated
   * and there is nothing to warn about".
   *
   * <p><b>Why the distinction exists as a type and not as a convention.</b> Rule 5 of the project
   * {@code CLAUDE.md} forbids an indistinguishable absence: a caller handed a bare empty list
   * cannot tell "no incoherence" from "the reading never happened", and the second is a
   * configuration problem the user has to be told about. Wave 5 of Phase 130 needs it because the
   * BSC-perspective update reports this list to the user; see
   * {@code BscPerspectivesUpdateResponseDTO.CoherenceCheck}.
   */
  public Optional<List<IncoherentLinkDTO>> findIncoherentLinksIfIdentityActive() {
    Optional<InstitutionalIdentityId> activeIdentityId = identityRepository.findActive()
        .map(identity -> identity.getId());
    if (activeIdentityId.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(evaluate(linkRepository.findByIdentityId(activeIdentityId.get())));
  }

  /**
   * The same question, restricted to the links in which {@code goalId} is the source or the
   * target. This is the form the goal-update entry point consumes: the change is local to one
   * goal, so warning about links that goal is not part of would be noise.
   *
   * <p><b>Consequence, written so it is not discovered while reading a result:</b> a goal with no
   * links at all yields an EMPTY list by construction. An empty list from this method therefore
   * says "nothing to warn about for THIS goal", and says nothing whatsoever about links between
   * other goals.
   */
  public List<IncoherentLinkDTO> findIncoherentLinksForGoal(StrategicGoalId goalId) {
    if (goalId == null) {
      return List.of();
    }
    Optional<InstitutionalIdentityId> activeIdentityId = identityRepository.findActive()
        .map(identity -> identity.getId());
    if (activeIdentityId.isEmpty()) {
      return List.of();
    }
    List<StrategyMapLink> touchingTheGoal = linkRepository.findByIdentityId(activeIdentityId.get())
        .stream()
        .filter(link -> goalId.equals(link.getSourceGoalId()) || goalId.equals(link.getTargetGoalId()))
        .toList();
    return evaluate(touchingTheGoal);
  }

  private List<IncoherentLinkDTO> evaluate(List<StrategyMapLink> links) {
    if (links == null || links.isEmpty()) {
      return List.of();
    }

    Map<String, Integer> orderByCode = new HashMap<>();
    List<BscPerspectiveConfig> configs = perspectiveConfigRepository.findAll();
    if (configs != null) {
      for (BscPerspectiveConfig config : configs) {
        orderByCode.put(config.getCode(), config.getDisplayOrder());
      }
    }

    List<IncoherentLinkDTO> incoherent = new ArrayList<>();
    for (StrategyMapLink link : links) {
      Optional<StrategicGoal> source = goalRepository.findById(link.getSourceGoalId());
      Optional<StrategicGoal> target = goalRepository.findById(link.getTargetGoalId());
      // A link whose ends cannot be loaded cannot be judged. It is left out rather than reported
      // on a guess: reporting it would put an unfounded warning in front of the user, which is
      // the same class of defect (an assertion the reading does not support) that this wave
      // exists to remove.
      if (source.isEmpty() || target.isEmpty()) {
        continue;
      }
      evaluateOne(link, source.get(), target.get(), orderByCode).ifPresent(incoherent::add);
    }
    return incoherent;
  }

  private Optional<IncoherentLinkDTO> evaluateOne(StrategyMapLink link, StrategicGoal source,
                                                  StrategicGoal target, Map<String, Integer> orderByCode) {
    String sourceCode = source.getPerspective() != null ? source.getPerspective().getCode() : null;
    String targetCode = target.getPerspective() != null ? target.getPerspective().getCode() : null;

    Integer sourceOrder = sourceCode != null ? orderByCode.get(sourceCode) : null;
    Integer targetOrder = targetCode != null ? orderByCode.get(targetCode) : null;

    // ALL the clauses a link violates are collected, and not just the first one found. A link can
    // break more than one at a time -- the two links stored in this environment join cancelled
    // goals AND can have their direction inverted by a reordering -- and a warning that stops at
    // the first cause would send the reader to fix one thing and meet the same link again for
    // another. The FIRST element is the one reported in the single-valued `reason` field, so the
    // collection order below is the order the create handler evaluates its own guards in.
    List<IncoherentLinkDTO.Reason> reasons = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();

    // Mirror of CreateStrategyMapLinkCommandHandler's STATUS guard (FIX-10 / A-125-01, wave 5 of
    // Phase 130), which is evaluated there right after the two goals are loaded -- hence first
    // here too. The handler REFUSES the pair; this REPORTS it. Naming which end is cancelled
    // rather than saying "one of them" is the same requirement the refusal message carries: a
    // reader told only that "a goal is cancelled" would have to open both to find out which.
    boolean sourceCancelled = !source.isActive();
    boolean targetCancelled = !target.isActive();
    if (sourceCancelled || targetCancelled) {
      reasons.add(IncoherentLinkDTO.Reason.GOAL_CANCELLED);
      if (sourceCancelled && targetCancelled) {
        descriptions.add("Os dois objetivos ligados estão cancelados.");
      } else if (sourceCancelled) {
        descriptions.add("O objetivo de origem está cancelado.");
      } else {
        descriptions.add("O objetivo de destino está cancelado.");
      }
    }

    // Mirror of CreateStrategyMapLinkCommandHandler, guard 7 (both perspectives defined) and its
    // perspectiveOrder() lookup, which refuses "Perspetiva não configurada: <code>". Where the
    // handler refuses, this reports -- with its own reason, so the caller can tell "the direction
    // is wrong" from "the order cannot even be read".
    if (sourceOrder == null || targetOrder == null) {
      reasons.add(IncoherentLinkDTO.Reason.PERSPECTIVE_NOT_CONFIGURED);
      descriptions.add(
          "A perspetiva de um dos objetivos não está configurada, pelo que a ordem de causa-efeito não pode ser verificada.");
    } else if (sourceOrder < targetOrder) {
      // Mirror of CreateStrategyMapLinkCommandHandler, guard 8: it refuses when
      // sourceOrder < targetOrder, i.e. it allows when the source order is NOT LOWER than the
      // target's. The operator is the whole rule -- inverting it here would make the product warn
      // about the opposite of what it refuses.
      //
      // Chained to the branch above and not evaluated on its own, because with an unreadable
      // order there is no comparison to make: the two are the SAME clause of the handler, taken
      // at two different points of failure, not two independent findings.
      reasons.add(IncoherentLinkDTO.Reason.CAUSE_EFFECT_ORDER_INVERTED);
      descriptions.add(
          "A perspetiva de origem está numa ordem inferior à da perspetiva de destino no fluxo causa-efeito do BSC.");
    }

    if (reasons.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(describe(link, source, target, sourceCode, targetCode, sourceOrder, targetOrder,
        reasons, String.join(" ", descriptions)));
  }

  private IncoherentLinkDTO describe(StrategyMapLink link, StrategicGoal source, StrategicGoal target,
                                     String sourceCode, String targetCode,
                                     Integer sourceOrder, Integer targetOrder,
                                     List<IncoherentLinkDTO.Reason> reasons, String reasonDesc) {
    IncoherentLinkDTO dto = new IncoherentLinkDTO();
    dto.setLinkId(link.getId().getValor().getValor());
    dto.setSourceGoalId(source.getId().getValor().getValor());
    dto.setSourceGoalTitle(source.getTitle());
    dto.setSourcePerspective(sourceCode);
    dto.setSourcePerspectiveOrder(sourceOrder);
    dto.setTargetGoalId(target.getId().getValor().getValor());
    dto.setTargetGoalTitle(target.getTitle());
    dto.setTargetPerspective(targetCode);
    dto.setTargetPerspectiveOrder(targetOrder);
    dto.setReason(reasons.get(0));
    dto.setReasons(List.copyOf(reasons));
    dto.setReasonDesc(reasonDesc);
    return dto;
  }
}

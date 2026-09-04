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
 * <p><b>The rule below is the MIRROR of {@code CreateStrategyMapLinkCommandHandler}</b> (its
 * perspective guard and its order guard, lines 77-86 as read on 2026-09-04). The two must say the
 * same thing for the same pair of perspectives. What keeps them together is NOT shared control
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
    Optional<InstitutionalIdentityId> activeIdentityId = identityRepository.findActive()
        .map(identity -> identity.getId());
    if (activeIdentityId.isEmpty()) {
      return List.of();
    }
    return evaluate(linkRepository.findByIdentityId(activeIdentityId.get()));
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

    // Mirror of CreateStrategyMapLinkCommandHandler, guard 7 (both perspectives defined) and its
    // perspectiveOrder() lookup, which refuses "Perspetiva não configurada: <code>". Where the
    // handler refuses, this reports -- with its own reason, so the caller can tell "the direction
    // is wrong" from "the order cannot even be read".
    if (sourceOrder == null || targetOrder == null) {
      return Optional.of(describe(link, source, target, sourceCode, targetCode, sourceOrder, targetOrder,
          IncoherentLinkDTO.Reason.PERSPECTIVE_NOT_CONFIGURED,
          "A perspetiva de um dos objetivos não está configurada, pelo que a ordem de causa-efeito não pode ser verificada."));
    }

    // Mirror of CreateStrategyMapLinkCommandHandler, guard 8: it refuses when
    // sourceOrder < targetOrder, i.e. it allows when the source order is NOT LOWER than the
    // target's. The operator is the whole rule -- inverting it here would make the product warn
    // about the opposite of what it refuses.
    if (sourceOrder < targetOrder) {
      return Optional.of(describe(link, source, target, sourceCode, targetCode, sourceOrder, targetOrder,
          IncoherentLinkDTO.Reason.CAUSE_EFFECT_ORDER_INVERTED,
          "A perspetiva de origem está numa ordem inferior à da perspetiva de destino no fluxo causa-efeito do BSC."));
    }

    return Optional.empty();
  }

  private IncoherentLinkDTO describe(StrategyMapLink link, StrategicGoal source, StrategicGoal target,
                                     String sourceCode, String targetCode,
                                     Integer sourceOrder, Integer targetOrder,
                                     IncoherentLinkDTO.Reason reason, String reasonDesc) {
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
    dto.setReason(reason);
    dto.setReasonDesc(reasonDesc);
    return dto;
  }
}

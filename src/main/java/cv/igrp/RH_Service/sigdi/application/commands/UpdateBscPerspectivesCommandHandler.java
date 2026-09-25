package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.BscPerspectiveItemDTO;
import cv.igrp.RH_Service.sigdi.application.dto.BscPerspectivesUpdateResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.IncoherentLinkDTO;
import cv.igrp.RH_Service.sigdi.application.service.StrategyLinkCoherencePolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Validates and persists the admin's edits to the 4 fixed BSC perspectives' label and display
 * order in a single call. Rows are always pre-existing (Flyway-seeded by V26) -- there is
 * deliberately no fallback-to-create branch for a missing row, since no operation exposed here
 * ever creates or deletes a row (CONTEXT.md: "sem operação exposta de criar/apagar linha").
 *
 * <h2>The PERSP-03 isolation, and how far wave 5 of Phase 130 crosses it</h2>
 *
 * <p><b>The isolation exists and it was CHOSEN, not forgotten.</b> Until wave 5 this javadoc said
 * "this handler never references any strategic-goal repository or aggregate (PERSP-03
 * isolation)", and the constructor took a single repository to match. The reason it was chosen is
 * that BSC perspectives are CONFIGURATION: a handler that edits configuration and also reaches
 * into the strategic-goal aggregate would be one change away from correcting goals on the user's
 * behalf, which is a decision configuration is not entitled to make.
 *
 * <p><b>What changed, and by which requirement.</b> FIX-10, finding A-126-05 (Média): the
 * perspective display order is editable, and the link handler refuses a source whose order is
 * LOWER than the target's -- so reordering perspectives can leave links that are already stored in
 * a state the rule in force would no longer allow to be created, with no warning and no check at
 * all. Silence there is precisely the defect.
 *
 * <p><b>How far the crossing goes -- and it goes no further.</b> READING, through
 * {@link StrategyLinkCoherencePolicy}, and reporting the result in the response. The constructor
 * still takes NO strategic-goal repository and NO link repository: what it takes is the policy,
 * and that indirection is what keeps the isolation legible -- this handler still knows of no goal
 * repository, and what crosses the boundary is one encapsulated read. Nothing is written, nothing
 * is refused, and the read happens after the save, never before it.
 *
 * <p><b>What remains forbidden here, and it is the larger half.</b> This handler does not create,
 * modify or delete any strategic goal or any link, and it never refuses a perspective update
 * because of one. Decision 2 of {@code 130-CONTEXT.md}: refusing would put the user in a deadlock
 * the product explains nowhere, and correcting would put a configuration handler in charge of
 * deciding about goals. It warns, and it saves.
 */
@Component
public class UpdateBscPerspectivesCommandHandler
    implements CommandHandler<UpdateBscPerspectivesCommand, ResponseEntity<BscPerspectivesUpdateResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateBscPerspectivesCommandHandler.class);

  private static final Set<String> EXPECTED_CODES =
      Set.of("FINANCIAL", "CUSTOMER", "PROCESS", "LEARNING");
  private static final List<Integer> EXPECTED_ORDERS = List.of(1, 2, 3, 4);

  private final BscPerspectiveConfigRepository repository;

  // The ONLY collaborator through which this handler sees anything about strategic goals, and it
  // is a read-only policy rather than a repository. See the PERSP-03 section of the class javadoc.
  private final StrategyLinkCoherencePolicy coherencePolicy;

  public UpdateBscPerspectivesCommandHandler(BscPerspectiveConfigRepository repository,
      StrategyLinkCoherencePolicy coherencePolicy) {
    this.repository = repository;
    this.coherencePolicy = coherencePolicy;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<BscPerspectivesUpdateResponseDTO> handle(UpdateBscPerspectivesCommand command) {
    LOGGER.debug("UpdateBscPerspectivesCommand: {}", command);

    List<BscPerspectiveItemDTO> items = command.getPerspectives();

    // Defensive input-shape guard: null body / wrong item count / missing code, label, or order
    // (including an over-length label) would otherwise NPE, reach the domain constructor's raw
    // IllegalArgumentException, or reach a raw DataIntegrityViolationException from the DB's
    // VARCHAR(60) column -- always surfaces as a clean, curated 400 instead (T-73-09). label is
    // included here (not just code/order) so a null/blank/over-length label never bypasses this
    // message (73-REVIEW.md WR-01).
    //
    // This guard is the SOLE validation gate for this endpoint -- deliberately, not merely
    // incidentally. A prior fix pass also added Bean Validation annotations
    // (@NotBlank/@Size/@NotNull) to BscPerspectiveItemDTO/BscPerspectivesUpdateRequestDTO, but
    // that regressed behavior: the controller's @Valid @RequestBody has no adjacent
    // BindingResult, so Spring MVC throws MethodArgumentNotValidException during argument
    // resolution -- before this method ever runs -- which made this guard's curated Portuguese
    // message unreachable on the real HTTP path in favor of GlobalExceptionHandler's generic
    // English "Validation Errors" shape (73-REVIEW.md WR-01, re-review). Those annotations were
    // reverted; do not re-add them without also solving that interaction.
    if (items == null || items.size() != EXPECTED_CODES.size()
        || items.stream().anyMatch(item -> item.getCode() == null
            || item.getOrder() == null
            || item.getLabel() == null || item.getLabel().isBlank()
            || item.getLabel().length() > 60)) {
      throw IgrpResponseStatusException.badRequest(
          "Todas as 4 perspetivas têm de indicar código, rótulo (máx. 60 caracteres) e posição válidos.");
    }

    // Redundant server-side check (CONTEXT.md "validação dupla", T-73-06): the 4 codes submitted
    // must be exactly the 4 fixed internal codes, each exactly once. Runs BEFORE the order check
    // and BEFORE any findByCode call, so a crafted duplicate/unknown-code payload never reaches
    // per-item lookups.
    Set<String> submittedCodes =
        items.stream().map(BscPerspectiveItemDTO::getCode).collect(Collectors.toSet());
    if (submittedCodes.size() != EXPECTED_CODES.size() || !submittedCodes.equals(EXPECTED_CODES)) {
      throw IgrpResponseStatusException.badRequest(
          "As 4 perspetivas têm de ser submetidas exatamente uma vez cada.");
    }

    // Redundant server-side check (CONTEXT.md "validação dupla", T-73-05): order must be exactly
    // the permutation {1,2,3,4} -- no gaps, no duplicates. This is the authoritative gate; the
    // frontend zod check is only a client-side pre-check.
    List<Integer> submittedOrders =
        items.stream().map(BscPerspectiveItemDTO::getOrder).sorted().toList();
    if (!submittedOrders.equals(EXPECTED_ORDERS)) {
      throw IgrpResponseStatusException.badRequest(
          "Cada perspetiva tem de ter uma posição única entre 1 e 4, sem lacunas.");
    }

    List<BscPerspectiveConfig> updated = items.stream()
        .map(item -> repository.findByCode(item.getCode())
            .orElseThrow(() -> IgrpResponseStatusException.badRequest(
                "Código de perspetiva desconhecido: " + item.getCode()))
            .update(item.getLabel(), item.getOrder()))
        .toList();

    // Sorted by position (73-REVIEW.md WR-04) to honor the same "ordenadas por posição" contract
    // GetBscPerspectivesQueryHandler already promises -- saveAll()'s output order otherwise
    // mirrors the request payload's item order, not displayOrder.
    List<BscPerspectiveItemDTO> saved = repository.saveAll(updated).stream()
        .map(c -> new BscPerspectiveItemDTO(c.getCode(), c.getLabel(), c.getDisplayOrder()))
        .sorted(Comparator.comparing(BscPerspectiveItemDTO::getOrder))
        .toList();

    // THE PERSP-03 CROSSING, AND IT IS A READ. FIX-10 / A-126-05.
    //
    // THE ORDER MATTERS AND IT IS NOT INCIDENTAL: this runs AFTER saveAll and never before it.
    // The question being asked is "what would the rule in force no longer allow to be created,
    // UNDER THE ORDERS JUST SAVED" -- asking it before the save would answer about the orders the
    // user is replacing, which is a different question with the same shape, and the reader of the
    // answer would have no way to tell which one was answered.
    //
    // The result is REPORTED. It never becomes a refusal and never becomes a correction: no goal
    // and no link is written here, by this handler or through this policy, which is itself
    // read-only by contract (StrategyLinkCoherencePolicyTest#policyNeverWritesToAnyRepositoryPort).
    //
    // Optional.empty() means there is no active institutional identity, so nothing could be read.
    // That is NOT "there is nothing to warn about", and the envelope keeps the two apart --
    // CoherenceCheck.NOT_EVALUATED_NO_ACTIVE_IDENTITY with a null list, versus EVALUATED with a
    // list that may legitimately be empty. Rule 5 of CLAUDE.md forbids the indistinguishable
    // absence, and its corollary forbids presenting an absence as legitimate unless its cause was
    // discriminated instead of presumed.
    Optional<List<IncoherentLinkDTO>> report = coherencePolicy.findIncoherentLinksIfIdentityActive();

    BscPerspectivesUpdateResponseDTO response = new BscPerspectivesUpdateResponseDTO();
    response.setPerspectives(saved);
    if (report.isPresent()) {
      response.setCoherenceCheck(BscPerspectivesUpdateResponseDTO.CoherenceCheck.EVALUATED);
      response.setIncoherentLinks(report.get());
    } else {
      response.setCoherenceCheck(
          BscPerspectivesUpdateResponseDTO.CoherenceCheck.NOT_EVALUATED_NO_ACTIVE_IDENTITY);
      response.setIncoherentLinks(null);
    }

    return ResponseEntity.ok(response);
  }
}

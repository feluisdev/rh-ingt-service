package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.BscPerspectiveItemDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import java.util.Comparator;
import java.util.List;
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
 * ever creates or deletes a row (CONTEXT.md: "sem operação exposta de criar/apagar linha"). This
 * handler never references any strategic-goal repository or aggregate (PERSP-03 isolation).
 */
@Component
public class UpdateBscPerspectivesCommandHandler
    implements CommandHandler<UpdateBscPerspectivesCommand, ResponseEntity<List<BscPerspectiveItemDTO>>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateBscPerspectivesCommandHandler.class);

  private static final Set<String> EXPECTED_CODES =
      Set.of("FINANCIAL", "CUSTOMER", "PROCESS", "LEARNING");
  private static final List<Integer> EXPECTED_ORDERS = List.of(1, 2, 3, 4);

  private final BscPerspectiveConfigRepository repository;

  public UpdateBscPerspectivesCommandHandler(BscPerspectiveConfigRepository repository) {
    this.repository = repository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<List<BscPerspectiveItemDTO>> handle(UpdateBscPerspectivesCommand command) {
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
    List<BscPerspectiveItemDTO> response = repository.saveAll(updated).stream()
        .map(c -> new BscPerspectiveItemDTO(c.getCode(), c.getLabel(), c.getDisplayOrder()))
        .sorted(Comparator.comparing(BscPerspectiveItemDTO::getOrder))
        .toList();

    return ResponseEntity.ok(response);
  }
}

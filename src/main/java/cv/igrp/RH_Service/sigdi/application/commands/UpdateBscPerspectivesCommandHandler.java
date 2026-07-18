package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.BscPerspectiveItemDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
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

    // Defensive input-shape guard: null body / wrong item count / missing code or order would
    // otherwise NPE inside the checks below -- always surfaces as a clean 400 instead (T-73-09).
    if (items == null || items.size() != EXPECTED_CODES.size()
        || items.stream().anyMatch(item -> item.getCode() == null || item.getOrder() == null)) {
      throw IgrpResponseStatusException.badRequest(
          "Todas as 4 perspetivas têm de indicar código e posição válidos.");
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

    List<BscPerspectiveItemDTO> response = repository.saveAll(updated).stream()
        .map(c -> new BscPerspectiveItemDTO(c.getCode(), c.getLabel(), c.getDisplayOrder()))
        .toList();

    return ResponseEntity.ok(response);
  }
}

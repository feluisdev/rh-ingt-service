package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wire DTO for a single BSC perspective's admin-editable label and display order.
 * <p>
 * Plain hand-written DTO -- no backing {@code .igrpstudio} manifest, no GENERATED header
 * (mirrors {@code ComboboxItemDTO}). The JSON field is literally {@code order} (not
 * {@code displayOrder}) to match the frontend contract locked for Phase 73; the
 * query/command handlers bridge {@code order} (DTO) &harr; {@code displayOrder} (domain).
 * <p>
 * Deliberately carries no Bean Validation annotations (73-REVIEW.md WR-01, re-review). A prior fix
 * pass added {@code @NotBlank}/{@code @Size}/{@code @NotNull} here, but the controller's
 * {@code @Valid @RequestBody} has no adjacent {@code BindingResult}, so Spring MVC throws
 * {@code MethodArgumentNotValidException} during argument resolution -- before the controller
 * method body (and {@code UpdateBscPerspectivesCommandHandler}'s own curated-message guard) ever
 * runs. That made the guard's clean Portuguese 400 unreachable on the real HTTP path in favor of
 * {@code GlobalExceptionHandler}'s generic English "Validation Errors" shape. The handler's guard
 * is the sole, authoritative validation gate for this endpoint -- do not re-add field-level Bean
 * Validation here without also teaching {@code GlobalExceptionHandler} to emit the same curated
 * shape, or wiring a {@code BindingResult} into the controller.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class BscPerspectiveItemDTO {
    private String code;
    private String label;
    private Integer order;
}

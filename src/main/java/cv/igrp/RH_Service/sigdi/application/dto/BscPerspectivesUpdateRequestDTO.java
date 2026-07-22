package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper DTO for the PUT request body: {@code { "perspectives": [ &lt;4 items&gt; ] } }.
 * <p>
 * Plain hand-written DTO -- no backing {@code .igrpstudio} manifest, no GENERATED header.
 * <p>
 * Deliberately carries no Bean Validation annotations (73-REVIEW.md WR-01, re-review). A prior fix
 * pass added {@code @Valid} + {@code @NotEmpty} here (to cascade into now-reverted item-level
 * annotations on {@link BscPerspectiveItemDTO}), but {@code @NotEmpty} alone is validated directly
 * by the controller's pre-existing {@code @Valid @RequestBody} -- with no adjacent
 * {@code BindingResult}, a null/empty {@code perspectives} would throw
 * {@code MethodArgumentNotValidException} during argument resolution, bypassing
 * {@code UpdateBscPerspectivesCommandHandler}'s own curated-message guard exactly like the
 * item-level annotations did. The handler's guard already rejects a null/wrong-size list with the
 * same curated message, so this field is left unannotated; see {@link BscPerspectiveItemDTO} for
 * the fuller rationale.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class BscPerspectivesUpdateRequestDTO {
    private List<BscPerspectiveItemDTO> perspectives;
}

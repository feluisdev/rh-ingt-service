package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper DTO for the PUT request body: {@code { "perspectives": [ &lt;4 items&gt; ] } }.
 * <p>
 * Plain hand-written DTO -- no backing {@code .igrpstudio} manifest, no GENERATED header.
 * <p>
 * {@code @Valid} on {@code perspectives} is required for the controller's own
 * {@code @Valid @RequestBody BscPerspectivesUpdateRequestDTO} to cascade into each
 * {@code BscPerspectiveItemDTO} element -- without it, that item-level Bean Validation is a
 * silent no-op (73-REVIEW.md WR-01).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class BscPerspectivesUpdateRequestDTO {
    @Valid
    @NotEmpty(message = "perspectives is mandatory")
    private List<BscPerspectiveItemDTO> perspectives;
}

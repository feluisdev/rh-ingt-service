package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
 * Bean Validation annotations below (73-REVIEW.md WR-01) mirror the wording already used by the
 * generated {@code BscPerspectiveConfigEntity} for the same 3 fields; they only take effect
 * because {@code BscPerspectivesUpdateRequestDTO.perspectives} cascades with {@code @Valid}. The
 * command handler's own defensive guard remains the authoritative, curated-message gate -- these
 * annotations are the "validação dupla" backend-side belt-and-suspenders layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class BscPerspectiveItemDTO {
    @NotBlank(message = "code is mandatory")
    private String code;

    @NotBlank(message = "label is mandatory")
    @Size(max = 60, message = "label must be at most 60 characters")
    private String label;

    @NotNull(message = "order is mandatory")
    private Integer order;
}

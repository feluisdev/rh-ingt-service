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

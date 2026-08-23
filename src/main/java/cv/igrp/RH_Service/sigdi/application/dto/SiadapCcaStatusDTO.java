package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for {@code GET api/v1/compliance/siadap/me/cca-status} (SIA-05, Phase 104).
 *
 * <p>The field is a {@code Boolean} wrapper, not a {@code boolean} primitive, on purpose: with
 * a primitive, Lombok would generate {@code isCca()} and Jackson would derive the JSON property
 * name {@code cca} from it, silently breaking the contract the frontend depends on
 * ({@code data.isCca}). The wrapper generates {@code getIsCca()}, which Jackson serializes as
 * {@code isCca} -- the same convention already used by {@code QuotaValidationResponseDTO.isValid}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class SiadapCcaStatusDTO {

  private Boolean isCca;
}

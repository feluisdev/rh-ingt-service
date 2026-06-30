package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.RH_Service.shared.application.dto.PageDTO;
import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@IgrpDTO
public class WrapperListPaaSubmissionPeriodDTO extends PageDTO {

    @Valid
    private List<PaaSubmissionPeriodResponseDTO> data = new ArrayList<>();
}

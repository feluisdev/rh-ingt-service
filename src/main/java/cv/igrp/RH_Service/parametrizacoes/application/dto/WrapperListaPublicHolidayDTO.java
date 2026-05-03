package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import cv.igrp.RH_Service.shared.application.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@IgrpDTO
public class WrapperListaPublicHolidayDTO extends PageDTO {
    private List<PublicHolidayResponseDTO> content = new ArrayList<>();
}

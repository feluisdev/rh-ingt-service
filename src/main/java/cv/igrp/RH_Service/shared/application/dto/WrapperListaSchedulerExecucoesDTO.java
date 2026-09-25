package cv.igrp.RH_Service.shared.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@IgrpDTO
public class WrapperListaSchedulerExecucoesDTO extends PageDTO {

    private List<SchedulerExecucaoResponseDTO> content = new ArrayList<>();
}

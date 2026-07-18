package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.BscPerspectiveItemDTO;
import cv.igrp.framework.core.domain.Command;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBscPerspectivesCommand implements Command {

  private List<BscPerspectiveItemDTO> perspectives;
}

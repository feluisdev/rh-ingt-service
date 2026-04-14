package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.WorkflowCommentDTO;
import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveTacticalActivityCommand implements Command {

  private WorkflowCommentDTO workflowcomment;

  @NotBlank(message = "The field <id> is required")
  private String id;
}

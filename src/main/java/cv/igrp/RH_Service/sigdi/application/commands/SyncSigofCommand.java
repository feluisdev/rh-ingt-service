package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.SyncSigofRequestDTO;
import cv.igrp.framework.core.domain.Command;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncSigofCommand implements Command {

  @NotNull
  @Valid
  private SyncSigofRequestDTO body;
}
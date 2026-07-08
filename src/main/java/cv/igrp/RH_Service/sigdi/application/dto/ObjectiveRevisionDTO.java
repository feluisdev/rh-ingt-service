package cv.igrp.RH_Service.sigdi.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectiveRevisionDTO {
    private String currentObjectiveText;
    private String revisionJustification;
    private String newObjectiveSmart;
    private String approvalStatus;
}

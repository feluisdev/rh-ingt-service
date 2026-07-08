package cv.igrp.RH_Service.sigdi.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementActionDTO {
    private String actionAgreed;
    private String responsible;
    private String deadline;
    private String neededSupport;
}

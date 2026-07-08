package cv.igrp.RH_Service.sigdi.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetencyObservationDTO {
    private String competencyCode;
    private String observedEvidence;
}

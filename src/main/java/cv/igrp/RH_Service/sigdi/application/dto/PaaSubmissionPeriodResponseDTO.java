package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PaaSubmissionPeriodResponseDTO {

    private UUID id;
    private String type;
    private String typeDesc;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String statusDesc;
    private Integer year;
    private Long daysRemaining;
    private String purpose;
    private String purposeDesc;
}

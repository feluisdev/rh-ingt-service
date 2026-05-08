package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProcessoDisciplinarDTO {
    private String id;
    private String funcionarioId;
    private String processNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String penalty;
    private LocalDate penaltyStartDate;
    private LocalDate penaltyEndDate;
    private String officialBulletin;
    private String notes;
}

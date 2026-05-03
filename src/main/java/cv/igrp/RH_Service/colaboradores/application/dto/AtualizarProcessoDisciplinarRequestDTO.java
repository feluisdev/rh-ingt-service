package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AtualizarProcessoDisciplinarRequestDTO {
    private String processNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String penalty;
    private LocalDate penaltyStartDate;
    private LocalDate penaltyEndDate;
    private String officialBulletin;
    private String notes;
    private UUID documentId;
}

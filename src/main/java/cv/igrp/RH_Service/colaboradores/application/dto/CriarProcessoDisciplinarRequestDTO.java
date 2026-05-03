package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CriarProcessoDisciplinarRequestDTO {

    private String processNumber;

    @NotNull(message = "A data de início é obrigatória")
    private LocalDate startDate;

    private LocalDate endDate;
    private String penalty;
    private LocalDate penaltyStartDate;
    private LocalDate penaltyEndDate;
    private String officialBulletin;
    private String notes;
    private UUID documentId;
}

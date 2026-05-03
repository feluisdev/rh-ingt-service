package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AtualizarFormacaoRequestDTO {
    private String name;
    private String institution;
    private String typeOptionKey;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationHours;
    private UUID documentId;
}

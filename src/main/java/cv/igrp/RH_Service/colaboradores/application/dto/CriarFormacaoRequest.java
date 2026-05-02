package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CriarFormacaoRequest {

    @NotBlank(message = "O nome da formação é obrigatório")
    private String name;

    private String institution;
    private String typeOptionKey;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationHours;
    private UUID documentId;
}

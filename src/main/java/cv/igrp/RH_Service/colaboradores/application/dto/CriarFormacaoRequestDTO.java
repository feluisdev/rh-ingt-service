package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CriarFormacaoRequestDTO {

    @NotBlank(message = "O nome da formação é obrigatório")
    private String name;

    private String institution;
    private String trainingType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationHours;
}

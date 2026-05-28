package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FormacaoDTO {
    private String id;
    private String funcionarioId;
    private String name;
    private String institution;
    private String trainingType;
    private String trainingTypeDesc;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationHours;
}

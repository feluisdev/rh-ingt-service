package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class QualificacaoResponseDTO {
    private String id;
    private String funcionarioId;
    private String level;
    private String courseName;
    private String institution;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean completed;
    private String completedDesc;
    private Boolean isActive;
    private String estadoDesc;
}

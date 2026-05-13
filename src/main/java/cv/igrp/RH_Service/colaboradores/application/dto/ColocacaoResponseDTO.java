package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ColocacaoResponseDTO {
    private String id;
    private String funcionarioId;
    private String unitId;
    private String jobId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private String isCurrentDesc;
    private Boolean isActive;
    private String estadoDesc;
    private String assignmentType;
    private String notes;
}

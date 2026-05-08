package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class QualificacaoRequestDTO {
    private String funcionarioId;
    @NotBlank
    private String level;
    private String courseName;
    private String institution;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean completed;
}

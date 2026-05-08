package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DependenteRequestDTO {
    private String funcionarioId;
    @NotBlank
    private String fullName;
    @NotBlank
    private String relationshipType;
    private LocalDate birthDate;
    private String nif;
}

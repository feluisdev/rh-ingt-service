package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EnquadramentoRequestDTO {
    @NotBlank
    private String funcionarioId;
    @NotBlank
    private String careerId;
    @NotBlank
    private String categoryId;
    @NotBlank
    private String gradeId;
    @NotBlank
    private String cargoId;
    private String functionId;
    @NotBlank
    private String unidadeOrganicaId;
    @NotNull
    private LocalDate dataInicio;
}

package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DependenteRequest {
    private String funcionarioId;
    @NotBlank
    private String nome;
    @NotBlank
    private String parentesco;
    private LocalDate dataNascimento;
    private String nif;
}

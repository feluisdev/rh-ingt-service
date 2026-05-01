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
public class ContratoRequest {
    private String funcionarioId;
    @NotBlank
    private String tipoContrato;
    @NotNull
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String numeroContrato;
}

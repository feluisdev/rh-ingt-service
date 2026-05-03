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
public class PedidoAusenciaRequestDTO {
    @NotBlank
    private String tipoAusenciaId;
    @NotNull
    private LocalDate dataInicio;
    @NotNull
    private LocalDate dataFim;
    private String motivo;
}

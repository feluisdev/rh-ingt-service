package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SaldoAusenciaRequestDTO {

    @NotNull
    private UUID tipoAusenciaId;

    @NotNull
    @Min(1)
    private Integer ano;

    @NotNull
    @Min(0)
    private Integer diasDireito;
}

package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TipoAusenciaRequest {
    @NotBlank
    private String nome;
    @NotBlank
    private String codigo;
    @NotNull
    private Boolean deductsBalance;
    @NotNull
    private Boolean requiresApproval;
    private Integer maxDaysPerYear;
    private String categoryOptionCkey;
}

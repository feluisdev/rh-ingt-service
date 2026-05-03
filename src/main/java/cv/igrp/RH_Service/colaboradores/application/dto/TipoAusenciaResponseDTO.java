package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TipoAusenciaResponseDTO {
    private String id;
    private String nome;
    private String codigo;
    private Boolean deductsBalance;
    private Boolean requiresApproval;
    private Integer maxDaysPerYear;
    private String categoryOptionCkey;
    private Boolean isActive;
}

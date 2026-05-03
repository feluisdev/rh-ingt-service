package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubtipoLicencaMobilidadeRequestDTO {
    @NotBlank
    private String nome;
    @NotBlank
    private String codigo;
    @NotBlank
    private String recordType;
    @NotNull
    private Boolean affectsPay;
    @NotNull
    private Boolean countsForSeniority;
    @NotNull
    private Boolean canSelfSubmit;
}

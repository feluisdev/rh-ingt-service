package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubtipoLicencaMobilidadeResponseDTO {
    private String id;
    private String nome;
    private String codigo;
    private String recordType;
    private Boolean affectsPay;
    private Boolean countsForSeniority;
    private Boolean canSelfSubmit;
    private Boolean isActive;
}

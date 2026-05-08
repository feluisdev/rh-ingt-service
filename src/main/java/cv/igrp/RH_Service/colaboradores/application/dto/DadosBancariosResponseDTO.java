package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DadosBancariosResponseDTO {
    private String id;
    private String funcionarioId;
    private String banco;
    private String numeroConta;
    private String iban;
    private String numeroSegurancaSocial;
    private Boolean isActive;
}

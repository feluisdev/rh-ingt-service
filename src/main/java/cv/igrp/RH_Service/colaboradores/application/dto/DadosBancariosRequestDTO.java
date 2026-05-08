package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DadosBancariosRequestDTO {
    private String funcionarioId;
    private String banco;
    private String numeroConta;
    private String iban;
    private String numeroSegurancaSocial;
}

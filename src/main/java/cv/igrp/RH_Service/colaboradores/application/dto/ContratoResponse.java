package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ContratoResponse {
    private String id;
    private String funcionarioId;
    private String tipoContrato;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String numeroContrato;
    private Boolean isActive;
}

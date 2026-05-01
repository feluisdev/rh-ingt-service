package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LicencaMobilidadeResponse {
    private String id;
    private String funcionarioId;
    private String subtipoId;
    private SubtipoLicencaMobilidadeResponse subtipo;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String entidadeDestino;
    private String despachoNumero;
    private String observacoes;
    private Boolean isActive;
}

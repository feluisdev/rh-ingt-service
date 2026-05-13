package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LicencaMobilidadeResponseDTO {
    private String id;
    private String funcionarioId;
    private String subtipoId;
    private SubtipoLicencaMobilidadeResponseDTO subtipo;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String entidadeDestino;
    private String despachoNumero;
    private String observacoes;
    private Boolean isActive;
    private String estadoDesc;
    private String status;
    private String destinationUnitId;
    private String justification;
    private String rejectionReason;
}

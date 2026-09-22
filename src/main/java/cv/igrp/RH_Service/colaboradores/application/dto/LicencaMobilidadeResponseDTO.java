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
    /** A decisão: PENDING · APPROVED · REJECTED · CANCELLED. Diz o que foi despachado. */
    private String status;
    /**
     * O período, hoje: POR_INICIAR · EM_CURSO · TERMINADA. Nulo quando o registo não está
     * deferido. São dois eixos — «deferida» não quer dizer «a decorrer», e é por confundir os
     * dois que se aprovava em Setembro uma licença de Outubro e a pessoa ficava de licença logo.
     */
    private String estadoPeriodo;
    private String destinationUnitId;
    private String destinationUnitName;
    private String destinationPositionId;
    private String justification;
    private String rejectionReason;
}

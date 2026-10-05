package cv.igrp.RH_Service.estrutura.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PositionResponseDTO {
    private String id;
    private String numeroLugar;
    private String jobId;
    private String jobNome;
    private String unidadeOrganicaId;
    private String unidadeNome;
    private String careerId;
    private String careerNome;
    private String categoryId;
    private String categoryNome;
    private String parentPositionId;
    private String managesUnitId;
    private String estado;
    /** Porque está no estado actual (ao congelar ou descongelar); nulo nos Lugares de antes da V59. */
    private String estadoMotivo;
    private String estadoDespacho;
    /** Data em que passou ao estado actual (yyyy-MM-dd). */
    private String estadoDesde;
    private String legalBase;
    private Boolean isActive;
    private Boolean foraDeGrelha;
    private Boolean ocupado;
    /** Vago, mas reservado para quem aguarda o contrato (BR-AF-23): não se pode dar a outra pessoa. */
    private Boolean reservado;
    private String reservadoParaFuncionarioId;
    private String reservadoParaNome;
}

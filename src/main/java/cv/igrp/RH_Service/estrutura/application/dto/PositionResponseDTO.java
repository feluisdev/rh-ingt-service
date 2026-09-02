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
    private String legalBase;
    private Boolean isActive;
    private Boolean foraDeGrelha;
    private Boolean ocupado;
}

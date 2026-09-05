package cv.igrp.RH_Service.estrutura.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PositionRequestDTO {
    private String numeroLugar;
    private String jobId;
    private String unidadeOrganicaId;
    private String careerId;
    private String categoryId;
    private String parentPositionId;
    private String managesUnitId;
    private String legalBase;
}

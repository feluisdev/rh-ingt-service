package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fase 133, plano 05 ({@code JAN-03}, metade de leitura): uma entrada por revisão da janela, com
 * o instantâneo que cada campo tinha nessa revisão -- não uma diferença já calculada. O cliente
 * (painel "Histórico de Alterações", 133-07) compara revisões consecutivas para desenhar
 * "Início: ~~anterior~~ -> novo".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PaaSubmissionPeriodRevisionDTO {

    private Long revision;
    private String revisionType; // INSERT | UPDATE | DELETE
    private LocalDateTime revisionDate;

    // Resolução (ver GetPaaSubmissionPeriodAuditQueryHandler): na revisão de criação usa-se
    // createdBy; nas restantes usa-se lastModifiedBy, com createdBy como recurso se aquele vier
    // nulo. Não é coincidência -- é a única forma de nunca devolver este campo vazio, dado que
    // lastModifiedBy só é preenchido a partir da primeira alteração.
    private String modifiedBy;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer year;
    private String status;
    private String type;
    private String purpose;
    private String purposeDesc;
}

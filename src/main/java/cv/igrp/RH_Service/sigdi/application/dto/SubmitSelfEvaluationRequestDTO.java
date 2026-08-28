package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class SubmitSelfEvaluationRequestDTO {

    // Fase 113 (SIA-06), 2026-08-26, decisao do operador. O @NotBlank daqui era
    // inalcancavel: o @Valid do ComplianceController corre ANTES do
    // request.setEvaluationId(id), pelo que qualquer cliente que nao mandasse o campo
    // no corpo levava 400 -- e a interface nunca o mandou. O id vem do caminho do URL e
    // e essa a fonte de verdade; este campo e transporte interno, preenchido pelo
    // controlador em todas as vias de construcao (verificado: nao ha outra).
    private String evaluationId;

    @NotNull(message = "selfEvaluationScore is mandatory")
    private BigDecimal selfEvaluationScore;
}

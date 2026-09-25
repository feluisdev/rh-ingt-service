package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AssignBudgetDTO {

    // A-135-2AA, 2026-09-09. O @NotNull daqui era inalcancavel: o @Valid do
    // TaticalController corre ANTES de assignBudget() executar
    // assignBudgetRequest.setActivityId(id), pelo que qualquer cliente que nao mandasse
    // o campo no corpo levava sempre 400 -- e a interface real (AssignBudgetModal) nunca
    // o manda, porque o id ja vem do caminho do URL. Mesmo precedente do
    // RecordObjectiveAchievementRequestDTO/SubmitSelfEvaluationRequestDTO (Fase 113,
    // SIA-06). Este campo e transporte interno, preenchido pelo controlador em todas as
    // vias de construcao (confirmado: nao ha outra) -- a fonte de verdade e o {id} do
    // caminho, nao o corpo.
    private UUID activityId;

    @NotNull(message = "Budget estimated is required")
    private BigDecimal budgetEstimated;

    @NotBlank(message = "Economic classifier is required")
    private String economicClassifier;
}

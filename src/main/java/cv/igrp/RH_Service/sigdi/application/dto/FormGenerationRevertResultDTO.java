// Mantido à mão, não gerado pelo iGRP Studio: DTO de saída do comando que desfaz um lote de
// geração de formulários (Fase 120, plano 03, PRZ-04), sem manifesto correspondente em
// .igrpstudio/sigdi/dto/. Mesmo precedente dos três DTOs de leitura da Fase 119.
package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class FormGenerationRevertResultDTO {

    private String periodId;

    private String batchId;

    // Código de FormGenerationBatchStatus -- REVERTED ou PARTIALLY_REVERTED, derivado por
    // FormGenerationBatch#markReverted.
    private String status;

    private int revertedCount;

    private int blockedCount;

    private LocalDateTime revertedAt;

    private String revertedBy;

    private List<FormGenerationRevertSkippedItemDTO> blocked = new ArrayList<>();
}

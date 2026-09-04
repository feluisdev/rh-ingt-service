// Mantido à mão, não gerado pelo iGRP Studio: DTO de saída da leitura em bloco de contagens de
// geração de formulários (Fase 119, PRZ-05/PRZ-07), sem manifesto correspondente em
// .igrpstudio/sigdi/dto/. Ver 119-05-PLAN.md, D-24.
package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class FormGenerationSummaryDTO {

    private String periodId;

    // Código de FormGenerationBatchStatus, ou "NOT_GENERATED" quando o período nunca teve lote
    // nenhum (D-27, 119-05-PLAN.md) -- estado distinto de uma lista vazia.
    private String status;

    // FormGenerationBatch.CREATES_FORMS ou FormGenerationBatch.READ_ONLY.
    private String generationMode;

    private int createdCount;

    private int failedCount;

    private int skippedCount;

    private int pendingCount;

    private LocalDateTime generatedAt;

    private String generatedBy;

    // true quando o lote mostrado é uma simulação (D-28) -- não há outro lote a mostrar.
    private boolean dryRun;

    // Fase 120, plano 03 (PRZ-04): nulo enquanto o lote não foi desfeito -- distinto de "não
    // apurável", que não existe aqui. FormGenerationDetailDTO é uma classe irmã, não uma
    // subclasse (ver o cabeçalho dela), por isso os mesmos quatro campos são replicados lá.
    private LocalDateTime revertedAt;

    private String revertedBy;

    private int revertedCount;

    private int revertBlockedCount;
}

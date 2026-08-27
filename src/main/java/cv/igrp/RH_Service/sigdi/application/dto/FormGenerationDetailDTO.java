// Mantido à mão, não gerado pelo iGRP Studio: DTO de saída da leitura completa do lote de
// geração de formulários de um período (Fase 119, PRZ-05/PRZ-07), sem manifesto correspondente
// em .igrpstudio/sigdi/dto/. Ver 119-05-PLAN.md, D-24 (o modal, aberto uma linha de cada vez).
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
public class FormGenerationDetailDTO {

    private String periodId;

    // Código de FormGenerationBatchStatus, ou "NOT_GENERATED" (D-27).
    private String status;

    private String generationMode;

    private int createdCount;

    private int failedCount;

    private int skippedCount;

    private int pendingCount;

    private LocalDateTime generatedAt;

    private String generatedBy;

    private boolean dryRun;

    private String purpose;

    private String purposeDesc;

    private String type;

    private String typeDesc;

    private Integer year;

    // "APPLICABLE" ou "NOT_APPLICABLE" -- o limite medido do PAA_BSC_OBJECTIVES (D-26,
    // 119-05-PLAN.md): StrategicGoalEntity não tem coluna de unidade orgânica, o rol de quem
    // ainda não submeteu não é apurável para essa finalidade.
    private String submissionCheck;

    private String batchId;

    private List<FormGenerationItemDTO> created = new ArrayList<>();

    private List<FormGenerationItemDTO> failed = new ArrayList<>();

    private List<FormGenerationItemDTO> skipped = new ArrayList<>();

    private List<FormGenerationItemDTO> pending = new ArrayList<>();
}

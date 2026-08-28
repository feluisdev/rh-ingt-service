package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Desfazer um lote de geração de formulários (Fase 120, plano 03, {@code PRZ-04}).
 * {@code batchId} é explícito e não inferido -- o handler confirma que pertence mesmo a
 * {@code periodId} antes de tocar em qualquer coisa (ver
 * {@code RevertFormGenerationBatchCommandHandler}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevertFormGenerationBatchCommand implements Command {
    private UUID periodId;
    private UUID batchId;
}

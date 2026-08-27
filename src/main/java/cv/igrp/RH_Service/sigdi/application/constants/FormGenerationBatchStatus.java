package cv.igrp.RH_Service.sigdi.application.constants;

import cv.igrp.framework.core.domain.IgrpEnum;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

/**
 * Estado final de um lote de geração de formulários ({@code t_form_generation_batch.status}),
 * derivado por {@link cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch#finish}
 * a partir dos contadores acumulados durante o varrimento. Molde: {@link EligibilitySkipReason}.
 *
 * <p>Quem escreve esta coluna é o plano 03 (o gerador); quem a lê é o plano 05 (o ecrã de
 * relatório) e a Fase 120 (a reversão de lotes).
 */
public enum FormGenerationBatchStatus implements IgrpEnum<String> {

    COMPLETED("COMPLETED", "Geração concluída sem falhas"),
    PARTIAL("PARTIAL", "Geração concluída com falhas em parte do lote"),
    FAILED("FAILED", "A geração falhou por inteiro"),
    NOTHING_TO_GENERATE("NOTHING_TO_GENERATE", "Esta finalidade não cria formulários; o lote regista apenas quem tem de agir"),
    DRY_RUN("DRY_RUN", "Simulação: regista o que seria criado, sem criar nada");

    private final String code;
    private final String description;

    FormGenerationBatchStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    private static final Map<String, FormGenerationBatchStatus> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(FormGenerationBatchStatus::getCode, Function.identity()));

    public static Optional<FormGenerationBatchStatus> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    public static FormGenerationBatchStatus fromCodeOrThrow(String code) {
        return fromCode(code).orElseThrow(() ->
                IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST,
                        "Código inválido para FormGenerationBatchStatus: " + code));
    }

    public static Map<String, String> codeDescriptionMap() {
        return CODE_MAP.values().stream()
                .collect(Collectors.toMap(FormGenerationBatchStatus::getCode, FormGenerationBatchStatus::getDescription));
    }
}

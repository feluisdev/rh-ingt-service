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
 * Motivos por que uma avaliação criada por um lote não é apagada ao desfazer esse lote
 * ({@code t_form_generation_batch_item.revert_skip_reason}). Molde: {@link EligibilitySkipReason}.
 *
 * <p>Este é um enum <strong>novo</strong>, não uma reutilização de {@link EligibilitySkipReason}
 * nem de {@code EvaluatorSkipReason}. Esses dois respondem "porque é que esta pessoa não entrou
 * no lote" -- uma pergunta sobre elegibilidade, decidida antes da geração. Este responde
 * "porque é que este formulário já criado não foi apagado" -- uma pergunta sobre reversão,
 * decidida depois da geração, contra o estado actual da avaliação. Partilhar o enum confundiria
 * as duas perguntas.
 */
public enum FormGenerationRevertSkipReason implements IgrpEnum<String> {

    PHASE_ADVANCED("PHASE_ADVANCED", "A avaliação já saiu da fase de contratualização"),
    OBJECTIVES_ALREADY_PROPOSED("OBJECTIVES_ALREADY_PROPOSED", "A avaliação continua em contratualização, mas já tem objetivos propostos ou em negociação"),
    EVALUATION_NOT_FOUND("EVALUATION_NOT_FOUND", "A avaliação já não existe");

    private final String code;
    private final String description;

    FormGenerationRevertSkipReason(String code, String description) {
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

    private static final Map<String, FormGenerationRevertSkipReason> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(FormGenerationRevertSkipReason::getCode, Function.identity()));

    public static Optional<FormGenerationRevertSkipReason> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    public static FormGenerationRevertSkipReason fromCodeOrThrow(String code) {
        return fromCode(code).orElseThrow(() ->
                IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST,
                        "Código inválido para FormGenerationRevertSkipReason: " + code));
    }

    public static Map<String, String> codeDescriptionMap() {
        return CODE_MAP.values().stream()
                .collect(Collectors.toMap(FormGenerationRevertSkipReason::getCode, FormGenerationRevertSkipReason::getDescription));
    }
}

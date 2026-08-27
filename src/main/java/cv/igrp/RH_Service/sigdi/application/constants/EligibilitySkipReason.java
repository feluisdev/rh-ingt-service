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
 * Motivos por que uma unidade orgânica é saltada ao resolver os responsáveis elegíveis
 * de um período de submissão. Molde: {@link PaaLevel}.
 */
public enum EligibilitySkipReason implements IgrpEnum<String> {

    UNIT_WITHOUT_RESPONSIBLE("UNIT_WITHOUT_RESPONSIBLE", "Unidade orgânica sem responsável definido"),
    // Este motivo existe porque t_unidade_organica.responsible_employee_id é um UUID nu
    // sem chave estrangeira (V32, Fase 109) e pode portanto apontar para um funcionário
    // apagado -- não é hipótese defensiva, é a consequência directa de uma decisão de
    // esquema já registada.
    RESPONSIBLE_NOT_FOUND("RESPONSIBLE_NOT_FOUND", "O responsável indicado não corresponde a nenhum funcionário"),
    UNIT_WITHOUT_ASSIGNED_EMPLOYEES("UNIT_WITHOUT_ASSIGNED_EMPLOYEES", "Unidade orgânica sem funcionários enquadrados no ano");

    private final String code;
    private final String description;

    EligibilitySkipReason(String code, String description) {
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

    private static final Map<String, EligibilitySkipReason> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(EligibilitySkipReason::getCode, Function.identity()));

    public static Optional<EligibilitySkipReason> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    public static EligibilitySkipReason fromCodeOrThrow(String code) {
        return fromCode(code).orElseThrow(() ->
                IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST,
                        "Código inválido para EligibilitySkipReason: " + code));
    }

    public static Map<String, String> codeDescriptionMap() {
        return CODE_MAP.values().stream()
                .collect(Collectors.toMap(EligibilitySkipReason::getCode, EligibilitySkipReason::getDescription));
    }
}

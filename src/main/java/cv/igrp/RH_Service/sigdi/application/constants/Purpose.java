package cv.igrp.RH_Service.sigdi.application.constants;

import cv.igrp.framework.core.domain.IgrpEnum;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

public enum Purpose implements IgrpEnum<String> {

    PAA_BSC_OBJECTIVES("PAA_BSC_OBJECTIVES", "Objetivos Estratégicos PAA/BSC", 1),
    PAA("PAA", "Plano de Atividades Anual", 2),
    SIADAP("SIADAP", "Avaliação de Desempenho (SIADAP)", 3),
    SIADAP_INTERIM("SIADAP_INTERIM", "Avaliação Intercalar SIADAP", 4),
    SIADAP_FINAL("SIADAP_FINAL", "Avaliação Final SIADAP", 5);

    private final String code;
    private final String description;
    // Fixed position in the annual PAA/SIADAP sequence (FASE-02) -- intentionally NOT
    // derived from Java's implicit ordinal(), and unrelated to StrategicGoal's
    // BSC-canvas position/coordinate concept (positionX/positionY).
    private final int position;

    Purpose(String code, String description, int position) {
        this.code = code;
        this.description = description;
        this.position = position;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public int getPosition() {
        return position;
    }

    private static final Map<String, Purpose> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(Purpose::getCode, Function.identity()));

    public static Optional<Purpose> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    public static Purpose fromCodeOrThrow(String code) {
        return fromCode(code).orElseThrow(() ->
                IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST,
                        "Invalid Purpose for this code: " + code));
    }

    public static Map<String, String> codeDescriptionMap() {
        return CODE_MAP.values().stream()
                .collect(Collectors.toMap(Purpose::getCode, Purpose::getDescription));
    }
}

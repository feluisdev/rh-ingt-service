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

    PAA("PAA", "Plano de Atividades Anual"),
    SIADAP("SIADAP", "Avaliação de Desempenho (SIADAP)");

    private final String code;
    private final String description;

    Purpose(String code, String description) {
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

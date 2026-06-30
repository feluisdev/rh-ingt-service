package cv.igrp.RH_Service.sigdi.application.constants;

import cv.igrp.framework.core.domain.IgrpEnum;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

public enum AcceptanceStatus implements IgrpEnum<String> {

    PENDING_ACCEPTANCE("PENDING_ACCEPTANCE", "Pendente de Aceitação"),
    ACCEPTED("ACCEPTED", "Aceite"),
    NEGOTIATING("NEGOTIATING", "Em Negociação"),
    TACITLY_ACCEPTED("TACITLY_ACCEPTED", "Aceite Tacitamente");

    private final String code;
    private final String description;

    AcceptanceStatus(String code, String description) {
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

    private static final Map<String, AcceptanceStatus> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(AcceptanceStatus::getCode, Function.identity()));

    public static Optional<AcceptanceStatus> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    public static AcceptanceStatus fromCodeOrThrow(String code) {
        return fromCode(code).orElseThrow(() ->
                IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST,
                        "Invalid AcceptanceStatus for this code: " + code));
    }

    public static Map<String, String> codeDescriptionMap() {
        return CODE_MAP.values().stream()
                .collect(Collectors.toMap(AcceptanceStatus::getCode, AcceptanceStatus::getDescription));
    }
}

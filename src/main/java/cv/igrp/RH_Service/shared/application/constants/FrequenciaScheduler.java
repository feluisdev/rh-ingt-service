package cv.igrp.RH_Service.shared.application.constants;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.IgrpEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Frequências com que a interface deixa agendar um job; cada uma corresponde a uma forma de cron. */
public enum FrequenciaScheduler implements IgrpEnum<String> {

    DIARIO("DIARIO", "Diário"),
    SEMANAL("SEMANAL", "Semanal"),
    QUINZENAL("QUINZENAL", "Quinzenal"),
    MENSAL("MENSAL", "Mensal"),
    TRIMESTRAL("TRIMESTRAL", "Trimestral"),
    SEMESTRAL("SEMESTRAL", "Semestral"),
    ANUAL("ANUAL", "Anual");

    private final String code;
    private final String description;

    FrequenciaScheduler(String code, String description) {
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

    private static final Map<String, FrequenciaScheduler> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(FrequenciaScheduler::getCode, Function.identity()));

    public static Optional<FrequenciaScheduler> fromCode(String code) {
        return Optional.ofNullable(code == null ? null : CODE_MAP.get(code));
    }

    public static FrequenciaScheduler fromCodeOrThrow(String code) {
        return fromCode(code).orElseThrow(() -> IgrpResponseStatusException.badRequest(
                "Frequência desconhecida. Escolha diária, semanal, quinzenal, mensal, trimestral, semestral ou anual."));
    }
}

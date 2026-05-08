package cv.igrp.RH_Service.shared.application.constants;

import cv.igrp.framework.core.domain.IgrpEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum RegimeTrabalho implements IgrpEnum<String> {

    TEMPO_COMPLETO("TEMPO_COMPLETO", "Jornada completa (35h/semana — LGTFP art. 123.º)"),
    TEMPO_PARCIAL("TEMPO_PARCIAL", "Jornada reduzida acordada entre as partes (LGTFP art. 128.º)"),
    ISENCAO_HORARIO("ISENCAO_HORARIO", "Sem horário fixo — cargos de direcção/chefia (LGTFP art. 126.º)"),
    DEDICACAO_EXCLUSIVA("DEDICACAO_EXCLUSIVA", "Proibição de acumular funções remuneradas (LGTFP art. 129.º)");

    private final String code;
    private final String description;

    RegimeTrabalho(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() { return code; }

    @Override
    public String getDescription() { return description; }

    private static final Map<String, RegimeTrabalho> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(RegimeTrabalho::getCode, Function.identity()));

    public static Optional<RegimeTrabalho> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    public static RegimeTrabalho fromCodeOrThrow(String code) {
        return fromCode(code).orElseThrow(() ->
                new IllegalArgumentException("Regime de trabalho inválido: " + code));
    }

    public static Map<String, String> codeDescriptionMap() {
        return CODE_MAP.values().stream()
                .collect(Collectors.toMap(RegimeTrabalho::getCode, RegimeTrabalho::getDescription));
    }
}

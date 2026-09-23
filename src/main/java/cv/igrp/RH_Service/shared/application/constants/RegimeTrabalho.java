package cv.igrp.RH_Service.shared.application.constants;

import cv.igrp.framework.core.domain.IgrpEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum RegimeTrabalho implements IgrpEnum<String> {

    // Lei n.º 20/X/2023. A duração do período normal e os regimes de prestação de trabalho ficam
    // para o diploma de desenvolvimento (arts. 164.º n.º 4 e 165.º n.º 4): não se escrevem aqui horas.
    TEMPO_COMPLETO("TEMPO_COMPLETO", "Horário completo (Lei n.º 20/X/2023, art. 164.º n.º 2)"),
    TEMPO_PARCIAL("TEMPO_PARCIAL", "Horário parcial, na percentagem acordada (Lei n.º 20/X/2023, art. 164.º n.º 2)"),
    ISENCAO_HORARIO("ISENCAO_HORARIO", "Isenção de horário — regime de prestação a regular pelo diploma de desenvolvimento (Lei n.º 20/X/2023, art. 165.º n.º 4)"),
    DEDICACAO_EXCLUSIVA("DEDICACAO_EXCLUSIVA", "Exclusividade — regra das funções públicas (Lei n.º 20/X/2023, art. 20.º)");

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

    public static String codigosValidos() {
        return Arrays.stream(values()).map(RegimeTrabalho::getCode).collect(Collectors.joining(", "));
    }
}

package cv.igrp.RH_Service.shared.application.constants;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.IgrpEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Dia da semana de um agendamento SEMANAL. O {@code code} é o que o cron do Spring aceita. */
public enum DiaDaSemana implements IgrpEnum<String> {

    SEG("MON", "Segunda-feira"),
    TER("TUE", "Terça-feira"),
    QUA("WED", "Quarta-feira"),
    QUI("THU", "Quinta-feira"),
    SEX("FRI", "Sexta-feira"),
    SAB("SAT", "Sábado"),
    DOM("SUN", "Domingo");

    private final String code;
    private final String description;

    DiaDaSemana(String code, String description) {
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

    private static final Map<String, DiaDaSemana> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(DiaDaSemana::getCode, Function.identity()));

    public static Optional<DiaDaSemana> fromCode(String code) {
        return Optional.ofNullable(code == null ? null : CODE_MAP.get(code));
    }

    public static DiaDaSemana fromCodeOrThrow(String code) {
        return fromCode(code).orElseThrow(() -> IgrpResponseStatusException.badRequest(
                "Dia da semana desconhecido. Escolha um dia de segunda-feira a domingo."));
    }
}

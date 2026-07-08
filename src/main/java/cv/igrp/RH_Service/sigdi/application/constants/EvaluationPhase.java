package cv.igrp.RH_Service.sigdi.application.constants;

import cv.igrp.framework.core.domain.IgrpEnum;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fases do ciclo de avaliação de desempenho (SIADAP).
 * <p>
 * O ciclo segue as fases definidas no DL 12/2020 de Cabo Verde:
 * contratualização → avaliação → harmonização → encerramento.
 */
public enum EvaluationPhase implements IgrpEnum<String> {

    /**
     * Ciclo aberto: fase de contratualização de objetivos entre avaliador e avaliado.
     */
    OPEN("OPEN", "Aberto — Contratualização"),

    /**
     * Ciclo em curso: objetivos contratualizados, a decorrer.
     */
    IN_PROGRESS("IN_PROGRESS", "Em Curso"),

    /**
     * Fase de autoavaliação: o colaborador submete a sua autoavaliação.
     */
    SELF_EVALUATION("SELF_EVALUATION", "Autoavaliação"),

    /**
     * Fase de avaliação pelo avaliador/gestor.
     */
    MANAGER_EVALUATION("MANAGER_EVALUATION", "Avaliação pelo Avaliador"),

    /**
     * Fase de harmonização: revisão pelo Conselho Coordenador da Avaliação (CCA).
     */
    HARMONIZATION("HARMONIZATION", "Harmonização"),

    /**
     * Ciclo encerrado: quotas validadas e avaliação finalizada.
     */
    CLOSED("CLOSED", "Encerrado");

    private final String code;
    private final String description;

    EvaluationPhase(String code, String description) {
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

    private static final Map<String, EvaluationPhase> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(EvaluationPhase::getCode, Function.identity()));

    public static Optional<EvaluationPhase> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    public static EvaluationPhase fromCodeOrThrow(String code) {
        return fromCode(code).orElseThrow(() ->
                IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST,
                        "Invalid EvaluationPhase for this code: " + code));
    }

    public static Map<String, String> codeDescriptionMap() {
        return CODE_MAP.values().stream()
                .collect(Collectors.toMap(EvaluationPhase::getCode, EvaluationPhase::getDescription));
    }
}

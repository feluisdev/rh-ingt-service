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
 * Motivos por que um colaborador elegível não recebe avaliador, ao resolver a derivação do
 * avaliador (Fase 119, decisão do operador de 2026-08-27). Enum distinto de
 * {@link EligibilitySkipReason} por decisão (D-06, 119-02-PLAN.md): aquele enum está
 * documentado como "motivos por que uma unidade orgânica é saltada" -- os motivos aqui são
 * ao nível do colaborador, dentro de uma unidade que já não foi saltada pela Fase 116.
 * Misturar os dois apagaria essa distinção e alteraria um ficheiro da Fase 116 sem
 * necessidade. Molde: {@link EligibilitySkipReason}.
 */
public enum EvaluatorSkipReason implements IgrpEnum<String> {

    UNIT_WITHOUT_RESPONSIBLE("UNIT_WITHOUT_RESPONSIBLE",
            "A unidade orgânica do colaborador não tem responsável definido"),
    TOP_UNIT_HEAD("TOP_UNIT_HEAD",
            "Dirige a unidade de topo, que não tem unidade-mãe -- o avaliador da direcção é trabalho próprio"),
    PARENT_UNIT_NOT_FOUND("PARENT_UNIT_NOT_FOUND",
            "A unidade-mãe indicada não corresponde a nenhuma unidade"),
    PARENT_UNIT_WITHOUT_RESPONSIBLE("PARENT_UNIT_WITHOUT_RESPONSIBLE",
            "A unidade-mãe não tem responsável definido"),
    PARENT_UNIT_RESPONSIBLE_IS_SELF("PARENT_UNIT_RESPONSIBLE_IS_SELF",
            "O responsável da unidade-mãe é o próprio colaborador");

    private final String code;
    private final String description;

    EvaluatorSkipReason(String code, String description) {
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

    private static final Map<String, EvaluatorSkipReason> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(EvaluatorSkipReason::getCode, Function.identity()));

    public static Optional<EvaluatorSkipReason> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    public static EvaluatorSkipReason fromCodeOrThrow(String code) {
        return fromCode(code).orElseThrow(() ->
                IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST,
                        "Código inválido para EvaluatorSkipReason: " + code));
    }

    public static Map<String, String> codeDescriptionMap() {
        return CODE_MAP.values().stream()
                .collect(Collectors.toMap(EvaluatorSkipReason::getCode, EvaluatorSkipReason::getDescription));
    }
}

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
 * Resultado de uma linha de {@code t_form_generation_batch_item} -- o que aconteceu a um
 * colaborador (ou a uma unidade saltada) dentro de um lote de geração. Molde:
 * {@link EligibilitySkipReason}.
 *
 * <p>Quem escreve esta coluna é o plano 03 (o gerador); quem a lê é o plano 05 (o ecrã de
 * relatório) e a Fase 120 (a reversão de lotes).
 *
 * <p>A coluna {@code t_form_generation_batch_item.skip_reason} guarda o código de
 * {@link EligibilitySkipReason} <strong>ou</strong> de {@code EvaluatorSkipReason} (plano 02)
 * -- e é por isso um {@code VARCHAR} livre, e não uma referência a um único enum.
 */
public enum FormGenerationOutcome implements IgrpEnum<String> {

    CREATED("CREATED", "Formulário criado"),
    WOULD_CREATE("WOULD_CREATE", "Seria criado -- simulação"),
    ALREADY_EXISTED("ALREADY_EXISTED", "Já existia formulário para este colaborador e ano"),
    FAILED("FAILED", "A criação falhou"),
    SKIPPED("SKIPPED", "Saltado"),
    PENDING("PENDING", "Tem de agir; esta finalidade não cria formulário");

    private final String code;
    private final String description;

    FormGenerationOutcome(String code, String description) {
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

    private static final Map<String, FormGenerationOutcome> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(FormGenerationOutcome::getCode, Function.identity()));

    public static Optional<FormGenerationOutcome> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    public static FormGenerationOutcome fromCodeOrThrow(String code) {
        return fromCode(code).orElseThrow(() ->
                IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST,
                        "Código inválido para FormGenerationOutcome: " + code));
    }

    public static Map<String, String> codeDescriptionMap() {
        return CODE_MAP.values().stream()
                .collect(Collectors.toMap(FormGenerationOutcome::getCode, FormGenerationOutcome::getDescription));
    }
}

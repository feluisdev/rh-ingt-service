package cv.igrp.RH_Service.parametrizacoes.domain.models;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum OptionCcode {

    MARITAL_STATUS("MARITAL_STATUS", "Estado Civil"),
    SEX("SEX", "Sexo"),
    NATIONALITY("NATIONALITY", "Nacionalidade"),
    UNIT_TYPE("UNIT_TYPE", "Tipo de Unidade Orgânica"),
    DOC_CATEGORY("DOC_CATEGORY", "Categoria de Documento"),
    LEAVE_CATEGORY("LEAVE_CATEGORY", "Categoria de Ausência"),
    QUALIFICATION_LEVEL("QUALIFICATION_LEVEL", "Nível de Habilitação"),
    RELATIONSHIP_TYPE("RELATIONSHIP_TYPE", "Tipo de Parentesco"),
    ISLAND("ISLAND", "Ilha de Cabo Verde"),
    CONCELHO("CONCELHO", "Concelho"),
    TRAINING_TYPE("TRAINING_TYPE", "Tipo de Formação"),
    CAREER_REGIME("CAREER_REGIME", "Regime de Carreira"),
    BANCO("BANCO", "Banco"),
    WORK_REGIME("WORK_REGIME", "Regime de Trabalho"),
    WORKER_STATE_REASON("WORKER_STATE_REASON", "Motivo de Mudança de Estado"),
    RECORD_TYPE("RECORD_TYPE", "Tipo de Registo de Licença/Mobilidade");

    private final String code;
    private final String description;

    OptionCcode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }

    public String getDescription() { return description; }

    private static final Map<String, OptionCcode> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(OptionCcode::getCode, Function.identity()));

    public static Optional<OptionCcode> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    public static String codigosValidos() {
        return Arrays.stream(values()).map(OptionCcode::getCode).collect(Collectors.joining(", "));
    }
}

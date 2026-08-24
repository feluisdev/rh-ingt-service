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
 * Categorias de competências para avaliação de desempenho (SIADAP / ReCAP adaptado).
 * <p>
 * Baseado no Referencial de Competências para a Administração Pública (ReCAP)
 * e nas orientações do SIADAP de Cabo Verde (DL 12/2020).
 */
public enum CompetencyCategory implements IgrpEnum<String> {

    /**
     * Competências comportamentais/nucleares — transversais a todos os trabalhadores.
     * Ex: Orientação para o Serviço Público, Trabalho em Equipa, Integridade.
     */
    BEHAVIORAL("BEHAVIORAL", "Comportamental / Nuclear"),

    /**
     * Competências organizacionais/funcionais — relacionadas com o exercício das funções.
     * Ex: Organização e Planeamento, Tomada de Decisão, Comunicação.
     */
    ORGANIZATIONAL("ORGANIZATIONAL", "Organizacional / Funcional"),

    /**
     * Competências técnicas/específicas — conhecimentos técnicos do cargo.
     * Ex: competências de área de especialidade, ferramentas específicas.
     */
    TECHNICAL("TECHNICAL", "Técnica / Específica"),

    /**
     * Competências de liderança — aplicáveis a dirigentes e cargos de chefia.
     * Ex: Liderança, Gestão da Organização, Visão Estratégica.
     */
    LEADERSHIP("LEADERSHIP", "Liderança / Direção");

    private final String code;
    private final String description;

    CompetencyCategory(String code, String description) {
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

    private static final Map<String, CompetencyCategory> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(CompetencyCategory::getCode, Function.identity()));

    public static Optional<CompetencyCategory> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    public static CompetencyCategory fromCodeOrThrow(String code) {
        return fromCode(code).orElseThrow(() ->
                IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST,
                        "Código inválido para CompetencyCategory: " + code));
    }

    public static Map<String, String> codeDescriptionMap() {
        return CODE_MAP.values().stream()
                .collect(Collectors.toMap(CompetencyCategory::getCode, CompetencyCategory::getDescription));
    }
}

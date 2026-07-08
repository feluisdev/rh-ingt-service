package cv.igrp.RH_Service.sigdi.domain.compliance.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.application.constants.CompetencyCategory;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.constants.SiadapMeritRating;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.CompetencyItem;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.IndividualObjective;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Agregado principal do módulo de Compliance — Avaliação de Desempenho (SIADAP).
 * <p>
 * Modelo rico que reflete os padrões do SIADAP de Cabo Verde (DL 12/2020) e
 * as boas práticas do ReCAP (Referencial de Competências para a Administração Pública).
 * <p>
 * Parâmetros de avaliação:
 * <ul>
 *   <li><b>Resultados</b>: objetivos individuais com escala 1 (não atingido) / 3 (atingido) / 5 (superado)</li>
 *   <li><b>Competências</b>: por categoria (comportamental, organizacional, técnica, liderança) com escala 1-5</li>
 * </ul>
 * <p>
 * Ponderação configurável (ex: 60% Resultados + 40% Competências).
 * Menções qualitativas calculadas automaticamente.
 */
@Getter
public class SiadapEvaluation {

    private final SiadapEvaluationId id;
    private final String employeeId;
    private final Integer year;

    /** Unidade orgânica do colaborador avaliado. */
    private final String organicUnitId;

    /** ID do avaliador (gestor direto). */
    private final String evaluatorId;

    /** Objetivos individuais contratualizados (3-7, somando 100% de peso). */
    private final List<IndividualObjective> objectives;

    /** Competências avaliadas por categoria. */
    private final List<CompetencyItem> competencies;

    /**
     * Peso dos resultados/objetivos na nota final (ex: 60 para 60%).
     * Configurado via SiadapConfig do ano fiscal.
     */
    private final BigDecimal resultsWeight;

    /**
     * Peso das competências na nota final (ex: 40 para 40%).
     * resultsWeight + competenciesWeight deve = 100.
     */
    private final BigDecimal competenciesWeight;

    /** Nota de autoavaliação submetida pelo colaborador (escala 1-5). Opcional. */
    private final BigDecimal selfEvaluationScore;

    /** Nota final calculada: média ponderada de resultados e competências. */
    private final BigDecimal finalScore;

    /** Menção qualitativa derivada da nota final. */
    private final SiadapMeritRating meritRating;

    /** Indica se as quotas de mérito superior foram validadas pelo CCA. */
    private final boolean validatedQuota;

    /** Fase atual do ciclo de avaliação. */
    private final EvaluationPhase phase;

    /**
     * Estado de aceitação da proposta de objetivos individuais (dupla-aceitação avaliador↔avaliado).
     * {@code null} antes de qualquer proposta ser efetuada.
     */
    private final AcceptanceStatus acceptanceStatus;

    private SiadapEvaluation(SiadapEvaluationId id, String employeeId, Integer year,
                              String organicUnitId, String evaluatorId,
                              List<IndividualObjective> objectives,
                              List<CompetencyItem> competencies,
                              BigDecimal resultsWeight, BigDecimal competenciesWeight,
                              BigDecimal selfEvaluationScore, BigDecimal finalScore,
                              SiadapMeritRating meritRating, boolean validatedQuota,
                              EvaluationPhase phase, AcceptanceStatus acceptanceStatus) {
        if (id == null) throw new IllegalArgumentException("id é obrigatório");
        if (employeeId == null || employeeId.isBlank()) throw new IllegalArgumentException("employeeId é obrigatório");
        if (year == null) throw new IllegalArgumentException("year é obrigatório");
        if (resultsWeight == null || competenciesWeight == null)
            throw new IllegalArgumentException("ponderações são obrigatórias");

        this.id = id;
        this.employeeId = employeeId;
        this.year = year;
        this.organicUnitId = organicUnitId;
        this.evaluatorId = evaluatorId;
        this.objectives = (objectives != null) ? new ArrayList<>(objectives) : new ArrayList<>();
        this.competencies = (competencies != null) ? new ArrayList<>(competencies) : new ArrayList<>();
        this.resultsWeight = resultsWeight;
        this.competenciesWeight = competenciesWeight;
        this.selfEvaluationScore = selfEvaluationScore;
        this.finalScore = finalScore;
        this.meritRating = meritRating;
        this.validatedQuota = validatedQuota;
        this.phase = (phase != null) ? phase : EvaluationPhase.OPEN;
        this.acceptanceStatus = acceptanceStatus;
    }

    // ============================================================
    // Factory Methods
    // ============================================================

    /**
     * Cria uma nova avaliação no início do ciclo.
     * Fase inicial: OPEN (contratualização de objetivos).
     */
    public static SiadapEvaluation create(String employeeId, Integer year,
                                          String organicUnitId, String evaluatorId,
                                          BigDecimal resultsWeight, BigDecimal competenciesWeight) {
        return new SiadapEvaluation(
                SiadapEvaluationId.gerarNovo(), employeeId, year,
                organicUnitId, evaluatorId,
                new ArrayList<>(), new ArrayList<>(),
                resultsWeight, competenciesWeight,
                null, null, null, false,
                EvaluationPhase.OPEN, null
        );
    }

    /** Reconstrói a partir da persistência. */
    public static SiadapEvaluation reconstruct(SiadapEvaluationId id, String employeeId, Integer year,
                                               String organicUnitId, String evaluatorId,
                                               List<IndividualObjective> objectives,
                                               List<CompetencyItem> competencies,
                                               BigDecimal resultsWeight, BigDecimal competenciesWeight,
                                               BigDecimal selfEvaluationScore, BigDecimal finalScore,
                                               SiadapMeritRating meritRating, boolean validatedQuota,
                                               EvaluationPhase phase, AcceptanceStatus acceptanceStatus) {
        return new SiadapEvaluation(id, employeeId, year, organicUnitId, evaluatorId,
                objectives, competencies, resultsWeight, competenciesWeight,
                selfEvaluationScore, finalScore, meritRating, validatedQuota, phase, acceptanceStatus);
    }

    // ============================================================
    // Behavioral Methods — Ciclo de Avaliação
    // ============================================================

    /**
     * Contratualiza (propõe/reenvia) os objetivos individuais para este ciclo.
     * Define/substitui os objetivos e marca o estado de aceitação como PENDING_ACCEPTANCE,
     * sem avançar a fase — a avaliação só avança para IN_PROGRESS após aceitação (ver
     * {@link #acceptObjectives()}), consistente com o fluxo de dupla-aceitação avaliador↔avaliado.
     * Validações: só pode ser proposto/reenviado enquanto a avaliação está OPEN; mínimo 3,
     * máximo 7 objetivos; pesos devem somar 100%.
     */
    public SiadapEvaluation contractualizeObjectives(List<IndividualObjective> newObjectives) {
        if (!EvaluationPhase.OPEN.equals(this.phase))
            throw IgrpResponseStatusException.badRequest(
                    "Objetivos só podem ser propostos/reenviados enquanto a avaliação está em contratualização (OPEN)");
        if (newObjectives == null || newObjectives.isEmpty())
            throw IgrpResponseStatusException.badRequest("Deve definir pelo menos um objetivo");
        if (newObjectives.size() < 3 || newObjectives.size() > 7)
            throw IgrpResponseStatusException.badRequest("O número de objetivos deve estar entre 3 e 7 (SIADAP)");

        BigDecimal totalWeight = newObjectives.stream()
                .map(IndividualObjective::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(new BigDecimal("100")) != 0)
            throw IgrpResponseStatusException.badRequest(
                    "A soma dos pesos dos objetivos deve ser exatamente 100%. Atual: " + totalWeight + "%");

        return new SiadapEvaluation(this.id, this.employeeId, this.year,
                this.organicUnitId, this.evaluatorId,
                newObjectives, this.competencies,
                this.resultsWeight, this.competenciesWeight,
                this.selfEvaluationScore, null, null, false,
                this.phase, AcceptanceStatus.PENDING_ACCEPTANCE);
    }

    /**
     * O avaliado aceita os objetivos propostos: transita o estado de aceitação para ACCEPTED
     * e avança a fase da avaliação de OPEN para IN_PROGRESS, atomicamente (CONTRACT-04).
     * Válido a partir de PENDING_ACCEPTANCE ou NEGOTIATING.
     */
    public SiadapEvaluation acceptObjectives() {
        if (!AcceptanceStatus.PENDING_ACCEPTANCE.equals(this.acceptanceStatus) &&
                !AcceptanceStatus.NEGOTIATING.equals(this.acceptanceStatus))
            throw IgrpResponseStatusException.badRequest(
                    "Apenas objetivos pendentes de aceitação ou em negociação podem ser aceites");

        return new SiadapEvaluation(this.id, this.employeeId, this.year,
                this.organicUnitId, this.evaluatorId,
                this.objectives, this.competencies,
                this.resultsWeight, this.competenciesWeight,
                this.selfEvaluationScore, this.finalScore, this.meritRating, this.validatedQuota,
                EvaluationPhase.IN_PROGRESS, AcceptanceStatus.ACCEPTED);
    }

    /**
     * O avaliado solicita negociação dos objetivos propostos: transita o estado de aceitação
     * para NEGOTIATING, mantendo a fase inalterada (OPEN). Válido apenas a partir de
     * PENDING_ACCEPTANCE.
     */
    public SiadapEvaluation negotiateObjectives() {
        if (!AcceptanceStatus.PENDING_ACCEPTANCE.equals(this.acceptanceStatus))
            throw IgrpResponseStatusException.badRequest(
                    "Apenas objetivos pendentes de aceitação podem iniciar negociação");
        return changeAcceptanceStatus(AcceptanceStatus.NEGOTIATING);
    }

    /**
     * Regista o resultado atingido para um objetivo específico.
     *
     * @param objectiveCode código do objetivo
     * @param achieved valor atingido
     * @param siadapScore 1 (não atingido), 3 (atingido), 5 (superado)
     */
    public SiadapEvaluation recordObjectiveAchievement(String objectiveCode, BigDecimal achieved, int siadapScore) {
        if (!EvaluationPhase.MANAGER_EVALUATION.equals(this.phase) &&
                !EvaluationPhase.IN_PROGRESS.equals(this.phase))
            throw IgrpResponseStatusException.badRequest("Resultados de objetivos só podem ser registados nas fases IN_PROGRESS ou MANAGER_EVALUATION");

        List<IndividualObjective> updated = this.objectives.stream()
                .map(obj -> objectiveCode.equals(obj.getCode()) ? obj.withAchievement(achieved, siadapScore) : obj)
                .collect(Collectors.toList());

        if (updated.stream().noneMatch(obj -> objectiveCode.equals(obj.getCode())))
            throw IgrpResponseStatusException.badRequest("Objetivo não encontrado: " + objectiveCode);

        return new SiadapEvaluation(this.id, this.employeeId, this.year,
                this.organicUnitId, this.evaluatorId,
                updated, this.competencies,
                this.resultsWeight, this.competenciesWeight,
                this.selfEvaluationScore, null, null, false, this.phase, this.acceptanceStatus);
    }

    /**
     * O colaborador submete a autoavaliação (score global de autoavaliação 1-5).
     * Transita para fase MANAGER_EVALUATION.
     */
    public SiadapEvaluation submitSelfEvaluation(BigDecimal selfScore) {
        if (!EvaluationPhase.SELF_EVALUATION.equals(this.phase))
            throw IgrpResponseStatusException.badRequest("Autoavaliação só pode ser submetida na fase SELF_EVALUATION");
        if (selfScore == null || selfScore.compareTo(BigDecimal.ONE) < 0 || selfScore.compareTo(new BigDecimal("5")) > 0)
            throw IgrpResponseStatusException.badRequest("A nota de autoavaliação deve estar entre 1 e 5");

        return new SiadapEvaluation(this.id, this.employeeId, this.year,
                this.organicUnitId, this.evaluatorId,
                this.objectives, this.competencies,
                this.resultsWeight, this.competenciesWeight,
                selfScore, this.finalScore, this.meritRating, false,
                EvaluationPhase.MANAGER_EVALUATION, this.acceptanceStatus);
    }

    /**
     * Avança para a fase de autoavaliação (iniciada pelo avaliador/RH).
     */
    public SiadapEvaluation openSelfEvaluationPhase() {
        if (!EvaluationPhase.IN_PROGRESS.equals(this.phase))
            throw IgrpResponseStatusException.badRequest("Só é possível abrir autoavaliação em ciclos IN_PROGRESS");
        return changePhase(EvaluationPhase.SELF_EVALUATION);
    }

    /**
     * Define as competências a avaliar (seleção pelo avaliador).
     * Pelo menos uma competência comportamental/nuclear é obrigatória.
     */
    public SiadapEvaluation setCompetencies(List<CompetencyItem> newCompetencies) {
        if (newCompetencies == null || newCompetencies.isEmpty())
            throw IgrpResponseStatusException.badRequest("Deve selecionar pelo menos uma competência");

        boolean hasBehavioral = newCompetencies.stream()
                .anyMatch(c -> CompetencyCategory.BEHAVIORAL.equals(c.getCategory()));
        if (!hasBehavioral)
            throw IgrpResponseStatusException.badRequest(
                    "Deve incluir pelo menos uma competência comportamental/nuclear (SIADAP)");

        return new SiadapEvaluation(this.id, this.employeeId, this.year,
                this.organicUnitId, this.evaluatorId,
                this.objectives, newCompetencies,
                this.resultsWeight, this.competenciesWeight,
                this.selfEvaluationScore, null, null, false, this.phase, this.acceptanceStatus);
    }

    /**
     * Avalia uma competência específica (pontuação 1-5).
     */
    public SiadapEvaluation evaluateCompetency(String competencyCode, int score) {
        if (!EvaluationPhase.MANAGER_EVALUATION.equals(this.phase) &&
                !EvaluationPhase.IN_PROGRESS.equals(this.phase))
            throw IgrpResponseStatusException.badRequest("Competências só podem ser avaliadas nas fases IN_PROGRESS ou MANAGER_EVALUATION");

        List<CompetencyItem> updated = this.competencies.stream()
                .map(c -> competencyCode.equals(c.getCompetencyCode()) ? c.withScore(score) : c)
                .collect(Collectors.toList());

        if (updated.stream().noneMatch(c -> competencyCode.equals(c.getCompetencyCode())))
            throw IgrpResponseStatusException.badRequest("Competência não encontrada: " + competencyCode);

        return new SiadapEvaluation(this.id, this.employeeId, this.year,
                this.organicUnitId, this.evaluatorId,
                this.objectives, updated,
                this.resultsWeight, this.competenciesWeight,
                this.selfEvaluationScore, null, null, false, this.phase, this.acceptanceStatus);
    }

    /**
     * Finaliza a avaliação: calcula nota final, atribui menção qualitativa.
     * Transita para HARMONIZATION.
     * Exige que todos os objetivos e competências estejam avaliados.
     */
    public SiadapEvaluation finalizeEvaluation() {
        if (!EvaluationPhase.MANAGER_EVALUATION.equals(this.phase))
            throw IgrpResponseStatusException.badRequest("A avaliação só pode ser finalizada na fase MANAGER_EVALUATION");

        boolean allObjectivesEvaluated = objectives.stream().allMatch(IndividualObjective::isEvaluated);
        if (!allObjectivesEvaluated)
            throw IgrpResponseStatusException.badRequest("Todos os objetivos devem ser avaliados antes de finalizar");

        boolean allCompetenciesEvaluated = !competencies.isEmpty() && competencies.stream().allMatch(CompetencyItem::isEvaluated);
        if (!allCompetenciesEvaluated)
            throw IgrpResponseStatusException.badRequest("Todas as competências devem ser avaliadas antes de finalizar");

        BigDecimal resultsScore = calculateResultsScore();
        BigDecimal competenciesScore = calculateCompetenciesScore();
        BigDecimal final_ = calculateFinalScore(resultsScore, competenciesScore);
        SiadapMeritRating merit = deriveMeritRating(final_);

        return new SiadapEvaluation(this.id, this.employeeId, this.year,
                this.organicUnitId, this.evaluatorId,
                this.objectives, this.competencies,
                this.resultsWeight, this.competenciesWeight,
                this.selfEvaluationScore, final_, merit, false,
                EvaluationPhase.HARMONIZATION, this.acceptanceStatus);
    }

    /**
     * Valida e encerra o ciclo (após harmonização pelo CCA).
     */
    public SiadapEvaluation markQuotaValidated() {
        if (!EvaluationPhase.HARMONIZATION.equals(this.phase))
            throw IgrpResponseStatusException.badRequest("Validação de quota só é possível na fase HARMONIZATION");
        return new SiadapEvaluation(this.id, this.employeeId, this.year,
                this.organicUnitId, this.evaluatorId,
                this.objectives, this.competencies,
                this.resultsWeight, this.competenciesWeight,
                this.selfEvaluationScore, this.finalScore, this.meritRating, true,
                EvaluationPhase.CLOSED, this.acceptanceStatus);
    }

    /** Atribui diretamente a menção de mérito (pelo avaliador/CCA). */
    public SiadapEvaluation assignMeritRating(SiadapMeritRating rating) {
        return new SiadapEvaluation(this.id, this.employeeId, this.year,
                this.organicUnitId, this.evaluatorId,
                this.objectives, this.competencies,
                this.resultsWeight, this.competenciesWeight,
                this.selfEvaluationScore, this.finalScore,
                rating, this.validatedQuota, this.phase, this.acceptanceStatus);
    }

    // ============================================================
    // Calculation Methods
    // ============================================================

    /**
     * Nota dos resultados: média ponderada dos objetivos.
     * Escala 1-5 (1 = não atingido, 3 = atingido, 5 = superado).
     */
    public BigDecimal calculateResultsScore() {
        if (objectives.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = objectives.stream()
                .map(IndividualObjective::getWeightedScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Nota das competências: média aritmética das pontuações de todas as competências.
     * Escala 1-5.
     */
    public BigDecimal calculateCompetenciesScore() {
        List<CompetencyItem> evaluated = competencies.stream()
                .filter(CompetencyItem::isEvaluated)
                .collect(Collectors.toList());
        if (evaluated.isEmpty()) return BigDecimal.ZERO;

        BigDecimal sum = evaluated.stream()
                .map(c -> new BigDecimal(c.getScore()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(evaluated.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Nota final: média ponderada de resultados e competências.
     * finalScore = (resultsScore * resultsWeight + competenciesScore * competenciesWeight) / 100
     */
    private BigDecimal calculateFinalScore(BigDecimal resultsScore, BigDecimal competenciesScore) {
        BigDecimal weightedResults = resultsScore.multiply(resultsWeight);
        BigDecimal weightedCompetencies = competenciesScore.multiply(competenciesWeight);
        return weightedResults.add(weightedCompetencies)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /**
     * Deriva a menção qualitativa SIADAP a partir da nota final.
     * <ul>
     *   <li>4.50 – 5.00 → EXCELLENT (sujeito a quota)</li>
     *   <li>4.00 – 4.49 → VERY_GOOD</li>
     *   <li>3.50 – 3.99 → GOOD</li>
     *   <li>2.00 – 3.49 → REGULAR</li>
     *   <li>1.00 – 1.99 → INADEQUATE</li>
     * </ul>
     */
    public static SiadapMeritRating deriveMeritRating(BigDecimal score) {
        if (score == null) return null;
        if (score.compareTo(new BigDecimal("4.50")) >= 0) return SiadapMeritRating.EXCELLENT;
        if (score.compareTo(new BigDecimal("4.00")) >= 0) return SiadapMeritRating.VERY_GOOD;
        if (score.compareTo(new BigDecimal("3.50")) >= 0) return SiadapMeritRating.GOOD;
        if (score.compareTo(new BigDecimal("2.00")) >= 0) return SiadapMeritRating.REGULAR;
        return SiadapMeritRating.INADEQUATE;
    }

    // ============================================================
    // Accessors (defensive copies)
    // ============================================================

    public List<IndividualObjective> getObjectives() {
        return Collections.unmodifiableList(objectives);
    }

    public List<CompetencyItem> getCompetencies() {
        return Collections.unmodifiableList(competencies);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private SiadapEvaluation changePhase(EvaluationPhase newPhase) {
        return new SiadapEvaluation(this.id, this.employeeId, this.year,
                this.organicUnitId, this.evaluatorId,
                this.objectives, this.competencies,
                this.resultsWeight, this.competenciesWeight,
                this.selfEvaluationScore, this.finalScore, this.meritRating, this.validatedQuota,
                newPhase, this.acceptanceStatus);
    }

    /**
     * Reconstrói a instância com um novo estado de aceitação, mantendo todos os restantes
     * campos (incluindo a fase) inalterados. Mirrors {@code TacticalActivity.changeAcceptanceStatus}.
     */
    private SiadapEvaluation changeAcceptanceStatus(AcceptanceStatus newAcceptanceStatus) {
        return new SiadapEvaluation(this.id, this.employeeId, this.year,
                this.organicUnitId, this.evaluatorId,
                this.objectives, this.competencies,
                this.resultsWeight, this.competenciesWeight,
                this.selfEvaluationScore, this.finalScore, this.meritRating, this.validatedQuota,
                this.phase, newAcceptanceStatus);
    }

    public boolean isClosed() {
        return EvaluationPhase.CLOSED.equals(this.phase);
    }

    public boolean isInProgress() {
        return EvaluationPhase.IN_PROGRESS.equals(this.phase);
    }
}

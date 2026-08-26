package cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.IndividualObjective;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SIA-03, critério 4 — primeiro teste de round-trip de mapeamento deste agregado. Prova que
 * {@code selfEvaluationTacitlyAccepted} atravessa {@code toDomain}/{@code toEntity}/{@code toDto}
 * sem se perder, reproduzindo literalmente a expressão que {@code GetEvaluationDetailQueryHandler}
 * usa para servir {@code GET siadap/evaluations/{id}}. É a classe de defeito que a decisão D-04
 * da Fase 100 evitou por pouco noutro campo: um campo correto no domínio que desaparece na
 * resposta HTTP sem que nenhum teste de domínio o apanhe.
 */
class SiadapEvaluationMapperTest {

    private static final Integer YEAR = 2026;

    private final SiadapEvaluationMapper mapper = new SiadapEvaluationMapper();

    private List<IndividualObjective> buildValidObjectives() {
        return List.of(
                IndividualObjective.create("OBJ-1", "Descrição do objetivo 1", "Indicador 1",
                        new BigDecimal("100"), new BigDecimal("40")),
                IndividualObjective.create("OBJ-2", "Descrição do objetivo 2", "Indicador 2",
                        new BigDecimal("100"), new BigDecimal("30")),
                IndividualObjective.create("OBJ-3", "Descrição do objetivo 3", "Indicador 3",
                        new BigDecimal("100"), new BigDecimal("30")));
    }

    /** Constrói o agregado por transições reais do domínio até MANAGER_EVALUATION por caminho
     * tácito — não por {@code reconstruct} forçado, para não representar um estado que o
     * domínio nunca produziria (mesma razão documentada no teste do agendador da Fase 102). */
    private SiadapEvaluation buildTacitlyAcceptedEvaluation() {
        return SiadapEvaluation.create(
                    UUID.randomUUID().toString(), YEAR,
                    UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                    new BigDecimal("60"), new BigDecimal("40"))
                .contractualizeObjectives(buildValidObjectives())
                .acceptObjectives()
                .openSelfEvaluationPhase()
                .applyTacitSelfEvaluationAcceptance();
    }

    private SiadapEvaluation buildOrdinarySubmittedEvaluation() {
        return SiadapEvaluation.create(
                    UUID.randomUUID().toString(), YEAR,
                    UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                    new BigDecimal("60"), new BigDecimal("40"))
                .contractualizeObjectives(buildValidObjectives())
                .acceptObjectives()
                .openSelfEvaluationPhase()
                .submitSelfEvaluation(new BigDecimal("4"));
    }

    @Test
    void toEntityThenToDto_preservesTacitAcceptanceFlag() {
        SiadapEvaluation domain = buildTacitlyAcceptedEvaluation();

        // Reproduz literalmente GetEvaluationDetailQueryHandler:46
        SiadapEvaluationDTO dto = mapper.toDto(mapper.toEntity(domain));

        assertTrue(dto.getSelfEvaluationTacitlyAccepted());
        assertNull(dto.getSelfEvaluationScore());
    }

    @Test
    void toDomainThenToEntity_roundTripsTacitAcceptanceFlag() {
        SiadapEvaluationEntity entity = new SiadapEvaluationEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmployeeId(UUID.randomUUID().toString());
        entity.setYear(YEAR.toString());
        entity.setOrganicUnitId(UUID.randomUUID().toString());
        entity.setEvaluatorId(UUID.randomUUID().toString());
        entity.setResultsWeight(new BigDecimal("60"));
        entity.setCompetenciesWeight(new BigDecimal("40"));
        entity.setEvaluationPhase(EvaluationPhase.MANAGER_EVALUATION.getCode());
        entity.setSelfEvaluationTacitlyAccepted(true);

        SiadapEvaluation domain = mapper.toDomain(entity, List.of(), List.of());
        assertTrue(domain.isSelfEvaluationTacitlyAccepted());

        SiadapEvaluationEntity roundTripped = mapper.toEntity(domain);
        assertTrue(roundTripped.isSelfEvaluationTacitlyAccepted());
    }

    @Test
    void toEntityThenToDto_leavesFlagFalseForOrdinarySubmission() {
        SiadapEvaluation domain = buildOrdinarySubmittedEvaluation();

        SiadapEvaluationDTO dto = mapper.toDto(mapper.toEntity(domain));

        assertFalse(dto.getSelfEvaluationTacitlyAccepted());
        assertEquals(new BigDecimal("4"), dto.getSelfEvaluationScore());
    }
}

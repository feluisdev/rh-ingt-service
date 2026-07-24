package cv.igrp.RH_Service.sigdi.application.queries;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QuotaUnitDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QuotaValidationResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QuotaViolationDTO;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapConfigEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapConfigEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapEvaluationEntityRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for {@link GetQuotaValidationQueryHandler} covering Phase 81 Plan 02's rewrite:
 * per-organic-unit grouping (including the null-unit group), config-driven Excelente/Bom quotas,
 * the minCollaboratorsForQuota gate, per-unit + aggregate compliance, and batch organic-unit name
 * resolution via a single {@link OrganicaLookupPort#findAllByIds} call.
 */
@ExtendWith(MockitoExtension.class)
class GetQuotaValidationQueryHandlerTest {

  private static final Integer YEAR = 2026;

  @Mock
  private SiadapEvaluationEntityRepository evaluationRepository;

  @Mock
  private SiadapConfigEntityRepository configRepository;

  @Mock
  private OrganicaLookupPort organicaLookupPort;

  @InjectMocks
  private GetQuotaValidationQueryHandler handler;

  // ============================================================
  // Fixture builders
  // ============================================================

  private SiadapEvaluationEntity evaluation(String organicUnitId, String meritRating) {
    SiadapEvaluationEntity entity = new SiadapEvaluationEntity();
    entity.setId(UUID.randomUUID());
    entity.setYear(YEAR.toString());
    entity.setOrganicUnitId(organicUnitId);
    entity.setMeritRating(meritRating);
    return entity;
  }

  private List<SiadapEvaluationEntity> nEvaluations(int count, String organicUnitId, String meritRating) {
    List<SiadapEvaluationEntity> list = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      list.add(evaluation(organicUnitId, meritRating));
    }
    return list;
  }

  private SiadapConfigEntity buildConfig(BigDecimal excellentQuota, BigDecimal goodQuota,
      Integer minCollaboratorsForQuota) {
    SiadapConfigEntity config = new SiadapConfigEntity();
    config.setFiscalYear(YEAR);
    config.setExcellentQuota(excellentQuota);
    config.setGoodQuota(goodQuota);
    config.setMinCollaboratorsForQuota(minCollaboratorsForQuota);
    return config;
  }

  // ============================================================
  // Behaviour 1: per-unit grouping, including the null key
  // ============================================================

  @Test
  void unitsGroupedByOrganicUnitIdIncludingNullKeyForUnassignedEvaluations() {
    String unitA = UUID.randomUUID().toString();
    String unitB = UUID.randomUUID().toString();
    List<SiadapEvaluationEntity> all = new ArrayList<>();
    all.addAll(nEvaluations(2, unitA, "REGULAR"));
    all.addAll(nEvaluations(3, unitB, "REGULAR"));
    all.addAll(nEvaluations(1, null, "REGULAR"));
    when(evaluationRepository.findByYear(YEAR.toString())).thenReturn(all);
    when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());
    when(organicaLookupPort.findAllByIds(any())).thenReturn(Map.of());

    ResponseEntity<QuotaValidationResponseDTO> response = handler.handle(new GetQuotaValidationQuery(YEAR));

    assertEquals(3, response.getBody().getUnits().size());
  }

  // ============================================================
  // Behaviour 2: null-unit group survives as "Sem Unidade Atribuída"
  // ============================================================

  @Test
  void nullOrganicUnitIdGroupSurvivesAsUnassignedUnitNotDropped() {
    List<SiadapEvaluationEntity> all = new ArrayList<>(nEvaluations(2, null, "REGULAR"));
    when(evaluationRepository.findByYear(YEAR.toString())).thenReturn(all);
    when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());
    when(organicaLookupPort.findAllByIds(any())).thenReturn(Map.of());

    ResponseEntity<QuotaValidationResponseDTO> response = handler.handle(new GetQuotaValidationQuery(YEAR));

    assertEquals(1, response.getBody().getUnits().size());
    QuotaUnitDTO unit = response.getBody().getUnits().get(0);
    assertNull(unit.getOrganicUnitId());
    assertEquals("Sem Unidade Atribuída", unit.getOrganicUnitName());
    assertEquals(2, unit.getTotalCollaborators());
  }

  // ============================================================
  // Behaviour 3: goodQuota read from config, not hardcoded 35%
  // ============================================================

  @Test
  void goodQuotaReadFromConfigNotHardcoded35Percent() {
    String unitA = UUID.randomUUID().toString();
    List<SiadapEvaluationEntity> all = new ArrayList<>();
    all.addAll(nEvaluations(4, unitA, "GOOD"));
    all.addAll(nEvaluations(6, unitA, "REGULAR"));
    when(evaluationRepository.findByYear(YEAR.toString())).thenReturn(all);
    // goodQuota = 20%: allowed = floor(20*10/100) = 2 -- distinct from the old hardcoded 35%,
    // which would floor(35*10/100) = 3. Asserting the exact allowed value (2, not 3) proves the
    // config value is genuinely read rather than falling through to the removed hardcode.
    when(configRepository.findByFiscalYear(YEAR))
        .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("20"), null)));
    when(organicaLookupPort.findAllByIds(any())).thenReturn(Map.of());

    ResponseEntity<QuotaValidationResponseDTO> response = handler.handle(new GetQuotaValidationQuery(YEAR));

    QuotaUnitDTO unit = response.getBody().getUnits().get(0);
    assertEquals(2, unit.getGoodAllowed());
    assertFalse(unit.getIsCompliant());
    assertTrue(unit.getViolations().stream()
        .anyMatch(v -> "GOOD".equals(v.getRating()) && v.getAllowed() == 2));
  }

  // ============================================================
  // Behaviour 4: per-unit compliance is independent across units
  // ============================================================

  @Test
  void perUnitComplianceIndependentAcrossUnits() {
    String compliantUnit = UUID.randomUUID().toString();
    String violatingUnit = UUID.randomUUID().toString();
    List<SiadapEvaluationEntity> all = new ArrayList<>();
    // compliantUnit: total 10, excellentQuota 25% -> allowed 2, assigned 2 -> compliant
    all.addAll(nEvaluations(2, compliantUnit, "EXCELLENT"));
    all.addAll(nEvaluations(8, compliantUnit, "REGULAR"));
    // violatingUnit: total 10, excellentQuota 25% -> allowed 2, assigned 4 -> violation
    all.addAll(nEvaluations(4, violatingUnit, "EXCELLENT"));
    all.addAll(nEvaluations(6, violatingUnit, "REGULAR"));
    when(evaluationRepository.findByYear(YEAR.toString())).thenReturn(all);
    when(configRepository.findByFiscalYear(YEAR))
        .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("35"), null)));
    when(organicaLookupPort.findAllByIds(any())).thenReturn(Map.of());

    ResponseEntity<QuotaValidationResponseDTO> response = handler.handle(new GetQuotaValidationQuery(YEAR));

    Map<String, QuotaUnitDTO> byId = response.getBody().getUnits().stream()
        .collect(Collectors.toMap(QuotaUnitDTO::getOrganicUnitId, u -> u));
    assertTrue(byId.get(compliantUnit).getIsCompliant());
    assertTrue(byId.get(compliantUnit).getViolations().isEmpty());
    assertFalse(byId.get(violatingUnit).getIsCompliant());
    assertFalse(byId.get(violatingUnit).getViolations().isEmpty());
  }

  // ============================================================
  // Behaviour 5: minCollaboratorsForQuota gate (+ disabled valve)
  // ============================================================

  @Test
  void minCollaboratorsBelowThresholdAddsViolationAndMarksNonCompliant() {
    String unitA = UUID.randomUUID().toString();
    List<SiadapEvaluationEntity> all = new ArrayList<>(nEvaluations(2, unitA, "REGULAR"));
    when(evaluationRepository.findByYear(YEAR.toString())).thenReturn(all);
    when(configRepository.findByFiscalYear(YEAR))
        .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("35"), 5)));
    when(organicaLookupPort.findAllByIds(any())).thenReturn(Map.of());

    ResponseEntity<QuotaValidationResponseDTO> response = handler.handle(new GetQuotaValidationQuery(YEAR));

    QuotaUnitDTO unit = response.getBody().getUnits().get(0);
    assertFalse(unit.getIsCompliant());
    QuotaViolationDTO violation = unit.getViolations().stream()
        .filter(v -> "MIN_COLLABORATORS".equals(v.getType()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("expected a MIN_COLLABORATORS violation"));
    assertTrue(violation.getMessage().contains("mínimo: 5"));
    assertTrue(violation.getMessage().contains("atual: 2"));
  }

  @Test
  void minCollaboratorsGateDisabledWhenConfigNullProducesNoViolation() {
    String unitA = UUID.randomUUID().toString();
    List<SiadapEvaluationEntity> all = new ArrayList<>(nEvaluations(2, unitA, "REGULAR"));
    when(evaluationRepository.findByYear(YEAR.toString())).thenReturn(all);
    when(configRepository.findByFiscalYear(YEAR))
        .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("35"), null)));
    when(organicaLookupPort.findAllByIds(any())).thenReturn(Map.of());

    ResponseEntity<QuotaValidationResponseDTO> response = handler.handle(new GetQuotaValidationQuery(YEAR));

    QuotaUnitDTO unit = response.getBody().getUnits().get(0);
    assertTrue(unit.getIsCompliant());
    assertTrue(unit.getViolations().isEmpty());
  }

  // ============================================================
  // Behaviour 6: aggregate isValid is the true aggregate of per-unit compliance
  // ============================================================

  @Test
  void aggregateIsValidFalseWhenAnyUnitNonCompliant() {
    String compliantUnit = UUID.randomUUID().toString();
    String violatingUnit = UUID.randomUUID().toString();
    List<SiadapEvaluationEntity> all = new ArrayList<>();
    all.addAll(nEvaluations(2, compliantUnit, "REGULAR"));
    all.addAll(nEvaluations(4, violatingUnit, "EXCELLENT"));
    all.addAll(nEvaluations(6, violatingUnit, "REGULAR"));
    when(evaluationRepository.findByYear(YEAR.toString())).thenReturn(all);
    when(configRepository.findByFiscalYear(YEAR))
        .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("35"), null)));
    when(organicaLookupPort.findAllByIds(any())).thenReturn(Map.of());

    ResponseEntity<QuotaValidationResponseDTO> response = handler.handle(new GetQuotaValidationQuery(YEAR));

    assertFalse(response.getBody().getIsValid());
  }

  @Test
  void aggregateIsValidTrueWhenAllUnitsCompliant() {
    String unitA = UUID.randomUUID().toString();
    String unitB = UUID.randomUUID().toString();
    List<SiadapEvaluationEntity> all = new ArrayList<>();
    all.addAll(nEvaluations(2, unitA, "REGULAR"));
    all.addAll(nEvaluations(3, unitB, "REGULAR"));
    when(evaluationRepository.findByYear(YEAR.toString())).thenReturn(all);
    when(configRepository.findByFiscalYear(YEAR))
        .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("35"), null)));
    when(organicaLookupPort.findAllByIds(any())).thenReturn(Map.of());

    ResponseEntity<QuotaValidationResponseDTO> response = handler.handle(new GetQuotaValidationQuery(YEAR));

    assertTrue(response.getBody().getIsValid());
  }

  // ============================================================
  // Behaviour 7: batch name resolution -- one findAllByIds call, never per-row findById
  // ============================================================

  @Test
  void organicUnitNamesResolvedViaSingleBatchFindAllByIdsCallNotPerRowFindById() {
    UUID unitAUuid = UUID.randomUUID();
    String unitA = unitAUuid.toString();
    List<SiadapEvaluationEntity> all = new ArrayList<>(nEvaluations(2, unitA, "REGULAR"));
    when(evaluationRepository.findByYear(YEAR.toString())).thenReturn(all);
    when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());

    OrganicaDTO organica = new OrganicaDTO();
    organica.setId(unitA);
    organica.setName("Direção de Recursos Humanos");
    when(organicaLookupPort.findAllByIds(any())).thenReturn(Map.of(unitAUuid, organica));

    ResponseEntity<QuotaValidationResponseDTO> response = handler.handle(new GetQuotaValidationQuery(YEAR));

    QuotaUnitDTO unit = response.getBody().getUnits().get(0);
    assertEquals("Direção de Recursos Humanos", unit.getOrganicUnitName());
    verify(organicaLookupPort, times(1)).findAllByIds(any());
    verify(organicaLookupPort, never()).findById(any());
  }

  @Test
  void malformedOrganicUnitIdFallsBackToRawIdWithoutThrowing() {
    String malformed = "not-a-valid-uuid";
    List<SiadapEvaluationEntity> all = new ArrayList<>(nEvaluations(1, malformed, "REGULAR"));
    when(evaluationRepository.findByYear(YEAR.toString())).thenReturn(all);
    when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());
    when(organicaLookupPort.findAllByIds(any())).thenReturn(Map.of());

    ResponseEntity<QuotaValidationResponseDTO> response = assertDoesNotThrow(
        () -> handler.handle(new GetQuotaValidationQuery(YEAR)));

    QuotaUnitDTO unit = response.getBody().getUnits().get(0);
    assertEquals(malformed, unit.getOrganicUnitName());
  }
}

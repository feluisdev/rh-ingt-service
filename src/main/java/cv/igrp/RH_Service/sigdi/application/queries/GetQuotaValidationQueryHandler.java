package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapConfigEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapConfigEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapEvaluationEntityRepository;
import cv.igrp.RH_Service.sigdi.application.dto.QuotaUnitDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QuotaValidationResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QuotaViolationDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GetQuotaValidationQueryHandler
    implements QueryHandler<GetQuotaValidationQuery, ResponseEntity<QuotaValidationResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetQuotaValidationQueryHandler.class);

  /** organicUnitName used for the null-organicUnitId group (81-RESEARCH.md Pitfall 4). */
  private static final String UNASSIGNED_UNIT_NAME = "Sem Unidade Atribuída";

  /**
   * Sentinel classifier key standing in for organicUnitId == null. Collectors.groupingBy's
   * accumulator explicitly rejects a null classification key -- throws NPE "element cannot be
   * mapped to a null key" -- even though the downstream HashMap itself would tolerate one as a
   * plain map key. This contradicts 81-RESEARCH.md Pattern 2 / 81-PATTERNS.md's literal code
   * snippet (both assumed groupingBy(getOrganicUnitId) directly over a null-containing stream
   * works); confirmed by a live failing test this session (Rule 1 fix). Never collides with a
   * real organicUnitId since it is not a valid UUID.
   */
  private static final String UNASSIGNED_KEY = "__UNASSIGNED__";

  private final SiadapEvaluationEntityRepository evaluationRepository;
  private final SiadapConfigEntityRepository configRepository;
  private final OrganicaLookupPort organicaLookupPort;

  public GetQuotaValidationQueryHandler(SiadapEvaluationEntityRepository evaluationRepository,
                                         SiadapConfigEntityRepository configRepository,
                                         OrganicaLookupPort organicaLookupPort) {
    this.evaluationRepository = evaluationRepository;
    this.configRepository = configRepository;
    this.organicaLookupPort = organicaLookupPort;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<QuotaValidationResponseDTO> handle(GetQuotaValidationQuery query) {
    LOGGER.debug("GetQuotaValidationQuery: {}", query);

    String year = query.getYear().toString();
    List<SiadapEvaluationEntity> all = evaluationRepository.findByYear(year);

    // Group by organicUnitId INCLUDING the null-unit case: evaluations with no unit must remain
    // visible as their own group, never filtered out before grouping (81-RESEARCH.md Pitfall 4).
    // A null classification key would throw inside groupingBy's accumulator (see UNASSIGNED_KEY
    // javadoc), so null is mapped to the UNASSIGNED_KEY sentinel here and mapped back to a real
    // null organicUnitId in buildUnit() below.
    Map<String, List<SiadapEvaluationEntity>> byUnit = all.stream()
        .collect(Collectors.groupingBy(e ->
            e.getOrganicUnitId() != null ? e.getOrganicUnitId() : UNASSIGNED_KEY));

    // Cross-aggregate config read — ONE lookup, reused for every unit's quota math plus the
    // min-collaborators gate (same 4-step map/filter/orElse chain used for excellentQuotaPct,
    // now applied to goodQuotaPct too, replacing the old hardcoded new BigDecimal("35")).
    Optional<SiadapConfigEntity> config = configRepository.findByFiscalYear(query.getYear());
    BigDecimal excellentQuotaPct = config.map(SiadapConfigEntity::getExcellentQuota)
        .filter(q -> q != null)
        .orElse(new BigDecimal("25"));
    BigDecimal goodQuotaPct = config.map(SiadapConfigEntity::getGoodQuota)
        .filter(q -> q != null)
        .orElse(new BigDecimal("35"));
    Integer minCollaborators = config.map(SiadapConfigEntity::getMinCollaboratorsForQuota).orElse(null);

    // Batch organic-unit name resolution (Pattern 3 / GetTaticalActivitiesQueryHandler analog) —
    // ONE organicaLookupPort.findAllByIds call over the non-null, UUID-parseable keys. Do NOT
    // resolve names per-row (that is ListSiadapEvaluationsQueryHandler's N+1 anti-pattern).
    Map<UUID, OrganicaDTO> organicaMap = resolveOrganicaNames(byUnit.keySet());

    List<QuotaUnitDTO> units = byUnit.entrySet().stream()
        .map(entry -> buildUnit(entry.getKey(), entry.getValue(), excellentQuotaPct, goodQuotaPct,
            minCollaborators, organicaMap))
        .toList();

    QuotaValidationResponseDTO response = new QuotaValidationResponseDTO();
    response.setYear(query.getYear());
    response.setUnits(units);
    // Aggregate isValid: true only when every unit is compliant (vacuously true when there are no
    // evaluations at all for the year — matches 81-UI-SPEC.md's "zero units -> compliant" rule).
    response.setIsValid(units.stream().allMatch(QuotaUnitDTO::getIsCompliant));

    return ResponseEntity.ok(response);
  }

  private Map<UUID, OrganicaDTO> resolveOrganicaNames(Set<String> unitKeys) {
    Set<UUID> unitIds = unitKeys.stream()
        .filter(Objects::nonNull)
        .map(this::tryParseUuid)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    return organicaLookupPort.findAllByIds(unitIds);
  }

  private UUID tryParseUuid(String value) {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      // A malformed stored id yields no resolved name rather than throwing (T-81-05: tolerant
      // parse, consistent with the existing pattern elsewhere in this module).
      return null;
    }
  }

  private QuotaUnitDTO buildUnit(String groupKey, List<SiadapEvaluationEntity> evaluations,
      BigDecimal excellentQuotaPct, BigDecimal goodQuotaPct, Integer minCollaborators,
      Map<UUID, OrganicaDTO> organicaMap) {

    // Map the UNASSIGNED_KEY sentinel back to a genuine null organicUnitId for the DTO/response
    // contract (frontend models organicUnitId as `string | null`, per 81-UI-SPEC.md).
    String organicUnitId = UNASSIGNED_KEY.equals(groupKey) ? null : groupKey;
    int total = evaluations.size();

    long excellentCount = evaluations.stream()
        .filter(e -> e.getMeritRating() != null && "EXCELLENT".equals(e.getMeritRating()))
        .count();
    long goodCount = evaluations.stream()
        .filter(e -> e.getMeritRating() != null && "GOOD".equals(e.getMeritRating()))
        .count();

    int excellentAllowed = total > 0
        ? excellentQuotaPct.multiply(new BigDecimal(total))
            .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR).intValue()
        : 0;
    int goodAllowed = total > 0
        ? goodQuotaPct.multiply(new BigDecimal(total))
            .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR).intValue()
        : 0;

    List<QuotaViolationDTO> violations = new ArrayList<>();

    // 1. minCollaboratorsForQuota gate — kept CONSISTENT with Plan 81-01's close-command gate
    // (same threshold semantics: null/0 = gate disabled) so the button-disable reason always
    // matches the close-block reason.
    if (minCollaborators != null && minCollaborators > 0 && total < minCollaborators) {
      QuotaViolationDTO v = new QuotaViolationDTO();
      v.setType("MIN_COLLABORATORS");
      v.setAllowed(minCollaborators);
      v.setAssigned(total);
      v.setMessage("Número insuficiente de avaliações para validar quotas (mínimo: "
          + minCollaborators + ", atual: " + total + ").");
      violations.add(v);
    }

    // 2. Excelente check — unchanged tone/shape from the pre-existing single-"Global"-unit code.
    if (excellentCount > excellentAllowed) {
      QuotaViolationDTO v = new QuotaViolationDTO();
      v.setType("QUOTA_EXCEEDED");
      v.setRating("EXCELLENT");
      v.setAllowed(excellentAllowed);
      v.setAssigned((int) excellentCount);
      v.setMessage("Excede a quota de Excelente em " + (excellentCount - excellentAllowed) + " colaborador(es).");
      violations.add(v);
    }

    // 3. Bom check — mirrors Excelente above, now config-driven (getGoodQuota) instead of the
    // previous hardcoded 35%. Independent of the Excelente outcome.
    if (goodCount > goodAllowed) {
      QuotaViolationDTO v = new QuotaViolationDTO();
      v.setType("QUOTA_EXCEEDED");
      v.setRating("GOOD");
      v.setAllowed(goodAllowed);
      v.setAssigned((int) goodCount);
      v.setMessage("Excede a quota de Bom em " + (goodCount - goodAllowed) + " colaborador(es).");
      violations.add(v);
    }

    QuotaUnitDTO unit = new QuotaUnitDTO();
    unit.setOrganicUnitId(organicUnitId);
    unit.setOrganicUnitName(resolveUnitName(organicUnitId, organicaMap));
    unit.setTotalCollaborators(total);
    unit.setExcellentAllowed(excellentAllowed);
    unit.setExcellentAssigned((int) excellentCount);
    unit.setGoodAllowed(goodAllowed);
    unit.setGoodAssigned((int) goodCount);
    unit.setIsCompliant(violations.isEmpty());
    unit.setViolations(violations);
    return unit;
  }

  private String resolveUnitName(String organicUnitId, Map<UUID, OrganicaDTO> organicaMap) {
    if (organicUnitId == null) {
      return UNASSIGNED_UNIT_NAME;
    }
    UUID uuid = tryParseUuid(organicUnitId);
    if (uuid == null) {
      return organicUnitId;
    }
    OrganicaDTO organica = organicaMap.get(uuid);
    return organica != null ? organica.getName() : organicUnitId;
  }
}

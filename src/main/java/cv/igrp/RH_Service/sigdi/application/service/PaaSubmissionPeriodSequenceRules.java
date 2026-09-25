package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Rule 3 (cross-type overlap, SOBREP-01/02/03) extracted from
 * CreatePaaSubmissionPeriodCommandHandler so creation and alteration (133-03, JAN-03) share a
 * single implementation instead of two divergent copies. The two-stage shape is load-bearing --
 * D-14, 133-CONTEXT.md -- and must not collapse into one pass:
 *
 * <ol>
 *   <li>dedupe by the key {@code position + ":" + type}, keeping the most recent record per pair
 *       (the caller's list already arrives ordered {@code createdDate DESC});</li>
 *   <li>group the survivors by position -- where UNIT_LEVEL and INDIVIDUAL_LEVEL can coexist --
 *       and only then pick nearestBefore as the one with the largest endDate immediately before,
 *       and nearestAfter as the one with the smallest startDate immediately after.</li>
 * </ol>
 *
 * A single-stage reading gives the same result on today's data and diverges the moment position 1
 * receives an INDIVIDUAL_LEVEL window alongside its UNIT_LEVEL sibling.
 */
@Component
public class PaaSubmissionPeriodSequenceRules {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * The nearest configured neighbors on each side of {@code newPosition}, exposed for callers
     * that want to inspect what was compared against. Production code calls
     * {@link #enforceNoOverlap} for the side effect of throwing on overlap; the returned pair is
     * informational only.
     */
    public record NearestNeighbors(PaaSubmissionPeriod nearestBefore, PaaSubmissionPeriod nearestAfter) {
    }

    /**
     * @param yearPeriods all periods for the target year (repository.findAllByYear), ordered
     *                    createdDate DESC
     * @param newPosition the sequence position (Purpose.getPosition()) of the window being
     *                    created or altered
     * @param startDate   the candidate startDate (new value on alteration)
     * @param endDate     the candidate endDate (new value on alteration)
     * @param excludeId   the id of the period being altered, excluded from the candidate set so a
     *                    window never overlaps itself; null on creation, where there is no id yet
     * @throws IgrpResponseStatusException 422 if the candidate range overlaps the nearest
     *                                      configured neighbor on either side
     */
    public NearestNeighbors enforceNoOverlap(List<PaaSubmissionPeriod> yearPeriods, int newPosition,
                                              LocalDate startDate, LocalDate endDate, UUID excludeId) {
        // Stage 1: dedupe by (position, type) key, most-recent-wins (D-14 step 1).
        Map<String, PaaSubmissionPeriod> latestByPositionAndType = new LinkedHashMap<>();
        for (PaaSubmissionPeriod p : yearPeriods) {
            if (excludeId != null && excludeId.equals(p.getId())) {
                continue;
            }
            String key = p.getPurpose().getPosition() + ":" + p.getType().getCode();
            latestByPositionAndType.putIfAbsent(key, p);
        }

        // Stage 2: group by position -- a position can carry two candidates (Unit + Individual)
        // -- and compare against the most restrictive one on each side (D-14 step 2).
        Map<Integer, List<PaaSubmissionPeriod>> candidatesByPosition = latestByPositionAndType.values().stream()
                .collect(Collectors.groupingBy(p -> p.getPurpose().getPosition()));

        PaaSubmissionPeriod nearestBefore = candidatesByPosition.keySet().stream()
                .filter(pos -> pos < newPosition)
                .max(Integer::compareTo)
                .flatMap(pos -> candidatesByPosition.get(pos).stream()
                        .max(Comparator.comparing(PaaSubmissionPeriod::getEndDate)))
                .orElse(null);

        PaaSubmissionPeriod nearestAfter = candidatesByPosition.keySet().stream()
                .filter(pos -> pos > newPosition)
                .min(Integer::compareTo)
                .flatMap(pos -> candidatesByPosition.get(pos).stream()
                        .min(Comparator.comparing(PaaSubmissionPeriod::getStartDate)))
                .orElse(null);

        if (nearestBefore != null && !startDate.isAfter(nearestBefore.getEndDate())) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Sobrepõe-se a " + nearestBefore.getPurpose().getDescription() + ", "
                            + nearestBefore.getStartDate().format(DATE_FORMAT) + "–" + nearestBefore.getEndDate().format(DATE_FORMAT));
        }
        if (nearestAfter != null && !nearestAfter.getStartDate().isAfter(endDate)) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Sobrepõe-se a " + nearestAfter.getPurpose().getDescription() + ", "
                            + nearestAfter.getStartDate().format(DATE_FORMAT) + "–" + nearestAfter.getEndDate().format(DATE_FORMAT));
        }

        return new NearestNeighbors(nearestBefore, nearestAfter);
    }
}

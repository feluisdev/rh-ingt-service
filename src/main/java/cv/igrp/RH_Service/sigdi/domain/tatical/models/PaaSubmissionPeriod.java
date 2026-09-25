package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PaaSubmissionPeriod {

    private final UUID id;
    private final Purpose purpose;
    private final PaaLevel type;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String status; // OPEN | CLOSED
    private final Integer year;
    // Read-only audit stamps mirrored from AuditEntity (created_date/created_by), both nullable
    // because a period just created in memory has no audit stamp yet. Not part of constructor
    // validation for that reason.
    private final LocalDateTime createdDate;
    private final String createdBy;

    private PaaSubmissionPeriod(UUID id, Purpose purpose, PaaLevel type, LocalDate startDate,
                                LocalDate endDate, String status, Integer year) {
        this(id, purpose, type, startDate, endDate, status, year, null, null);
    }

    private PaaSubmissionPeriod(UUID id, Purpose purpose, PaaLevel type, LocalDate startDate,
                                LocalDate endDate, String status, Integer year,
                                LocalDateTime createdDate, String createdBy) {
        if (purpose == null) throw new IllegalArgumentException("purpose é obrigatório");
        if (type == null) throw new IllegalArgumentException("type é obrigatório");
        if (startDate == null) throw new IllegalArgumentException("startDate é obrigatório");
        if (endDate == null) throw new IllegalArgumentException("endDate é obrigatório");
        if (startDate.isAfter(endDate))
            throw new IllegalArgumentException("startDate deve ser anterior a endDate");
        if (year == null) throw new IllegalArgumentException("year é obrigatório");

        this.id = id;
        this.purpose = purpose;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = (status != null) ? status : "OPEN";
        this.year = year;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
    }

    public static PaaSubmissionPeriod create(Purpose purpose, PaaLevel type, LocalDate startDate,
                                              LocalDate endDate, Integer year) {
        return new PaaSubmissionPeriod(UUID.randomUUID(), purpose, type, startDate, endDate, "OPEN", year);
    }

    public static PaaSubmissionPeriod reconstruct(UUID id, Purpose purpose, PaaLevel type, LocalDate startDate,
                                                   LocalDate endDate, String status, Integer year) {
        return reconstruct(id, purpose, type, startDate, endDate, status, year, null, null);
    }

    // Overload added instead of changing the 7-arg signature: 44 call sites measured across 13
    // files (11 of them tests) rely on it. createdDate/createdBy are read-only here — the only
    // writer of those columns is AuditingEntityListener over updatable=false columns; this
    // overload never feeds back into PaaSubmissionPeriodMapper.toEntity (see mapper comment).
    public static PaaSubmissionPeriod reconstruct(UUID id, Purpose purpose, PaaLevel type, LocalDate startDate,
                                                   LocalDate endDate, String status, Integer year,
                                                   LocalDateTime createdDate, String createdBy) {
        return new PaaSubmissionPeriod(id, purpose, type, startDate, endDate, status, year, createdDate, createdBy);
    }

    public PaaSubmissionPeriod close() {
        if ("CLOSED".equals(this.status))
            throw IgrpResponseStatusException.badRequest("Período já está fechado");
        return new PaaSubmissionPeriod(this.id, this.purpose, this.type, this.startDate, this.endDate, "CLOSED",
                this.year, this.createdDate, this.createdBy);
    }

    // 133-03 / JAN-03 (D-18, D-19): the only alteration path this entity has, and it exists
    // precisely because the entity is otherwise immutable by construction. purpose and type are
    // deliberately NOT parameters -- changing WHICH window this is would not be an alteration,
    // it would be creating a different window (133-UI-SPEC.md Appendix B). status, createdDate
    // and createdBy are carried over unchanged, exactly as close() already does; the caller
    // (UpdatePaaSubmissionPeriodCommandHandler) is the one that guards status via isOpen()
    // before ever calling this.
    public PaaSubmissionPeriod changeSchedule(LocalDate newStartDate, LocalDate newEndDate, Integer newYear) {
        // Guards against the constructor's IllegalArgumentException surfacing as a 500 -- the
        // same invariant (startDate before endDate) re-checked here so the failure is a 422
        // ProblemDetail instead.
        if (newStartDate != null && newEndDate != null && newStartDate.isAfter(newEndDate)) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "startDate deve ser anterior a endDate");
        }
        return new PaaSubmissionPeriod(this.id, this.purpose, this.type, newStartDate, newEndDate, this.status,
                newYear, this.createdDate, this.createdBy);
    }

    public boolean isOpen() {
        return "OPEN".equals(this.status);
    }

    public boolean isClosed() {
        return "CLOSED".equals(this.status);
    }

    public boolean isActiveToday() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        return isOpen() && !today.isBefore(startDate) && !today.isAfter(endDate);
    }
}

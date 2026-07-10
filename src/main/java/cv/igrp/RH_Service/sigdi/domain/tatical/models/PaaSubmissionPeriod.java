package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import lombok.Getter;

import java.time.LocalDate;
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

    private PaaSubmissionPeriod(UUID id, Purpose purpose, PaaLevel type, LocalDate startDate,
                                LocalDate endDate, String status, Integer year) {
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
    }

    public static PaaSubmissionPeriod create(Purpose purpose, PaaLevel type, LocalDate startDate,
                                              LocalDate endDate, Integer year) {
        return new PaaSubmissionPeriod(UUID.randomUUID(), purpose, type, startDate, endDate, "OPEN", year);
    }

    /** Backward-compatible overload — defaults purpose to PAA for callers not yet updated. */
    public static PaaSubmissionPeriod create(PaaLevel type, LocalDate startDate,
                                              LocalDate endDate, Integer year) {
        return create(Purpose.PAA, type, startDate, endDate, year);
    }

    public static PaaSubmissionPeriod reconstruct(UUID id, Purpose purpose, PaaLevel type, LocalDate startDate,
                                                   LocalDate endDate, String status, Integer year) {
        return new PaaSubmissionPeriod(id, purpose, type, startDate, endDate, status, year);
    }

    public PaaSubmissionPeriod close() {
        if ("CLOSED".equals(this.status))
            throw IgrpResponseStatusException.badRequest("Período já está fechado");
        return new PaaSubmissionPeriod(this.id, this.purpose, this.type, this.startDate, this.endDate, "CLOSED", this.year);
    }

    public boolean isOpen() {
        return "OPEN".equals(this.status);
    }

    public boolean isClosed() {
        return "CLOSED".equals(this.status);
    }

    public boolean isActiveToday() {
        LocalDate today = LocalDate.now();
        return isOpen() && !today.isBefore(startDate) && !today.isAfter(endDate);
    }
}

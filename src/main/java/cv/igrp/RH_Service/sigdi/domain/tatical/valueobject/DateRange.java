package cv.igrp.RH_Service.sigdi.domain.tatical.valueobject;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DateRange {

  private final LocalDate startDate;
  private final LocalDate endDate;

  private DateRange(LocalDate startDate, LocalDate endDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("startDate é obrigatório");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("endDate é obrigatório");
    }
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("endDate não pode ser anterior ao startDate");
    }
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public static DateRange of(LocalDate startDate, LocalDate endDate) {
    return new DateRange(startDate, endDate);
  }

  public boolean isActive(LocalDate referenceDate) {
    return !referenceDate.isBefore(startDate) && !referenceDate.isAfter(endDate);
  }

  public boolean isActive() {
    return isActive(LocalDate.now(AppTimeZone.CABO_VERDE));
  }

  public long durationInDays() {
    return startDate.until(endDate).getDays();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DateRange that)) return false;
    return startDate.equals(that.startDate) && endDate.equals(that.endDate);
  }

  @Override
  public int hashCode() {
    return 31 * startDate.hashCode() + endDate.hashCode();
  }

  @Override
  public String toString() {
    return startDate + " → " + endDate;
  }
}

package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.PublicHolidayId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;

@Getter
public class PublicHoliday {

    private PublicHolidayId id;
    private String name;
    private LocalDate holidayDate;
    private boolean national;
    private String description;
    private boolean active;

    private PublicHoliday() {}

    private PublicHoliday(PublicHolidayId id, String name, LocalDate holidayDate,
                          boolean national, String description, boolean active) {
        this.id = id;
        this.name = name;
        this.holidayDate = holidayDate;
        this.national = national;
        this.description = description;
        this.active = active;
    }

    public static PublicHoliday criar(String name, LocalDate holidayDate, boolean national, String description) {
        Objects.requireNonNull(name, "name não pode ser nulo");
        Objects.requireNonNull(holidayDate, "holidayDate não pode ser nulo");
        return new PublicHoliday(PublicHolidayId.gerarNovo(), name, holidayDate, national, description, true);
    }

    public static PublicHoliday reconstruir(PublicHolidayId id, String name, LocalDate holidayDate,
                                            boolean national, String description, boolean active) {
        return new PublicHoliday(id, name, holidayDate, national, description, active);
    }

    public void atualizar(String name, LocalDate holidayDate, boolean national, String description) {
        this.name = name;
        this.holidayDate = holidayDate;
        this.national = national;
        this.description = description;
    }

    public void desativar() {
        if (!this.active) throw IgrpResponseStatusException.conflict("Feriado já está inactivo.");
        this.active = false;
    }

    public void reativar() {
        if (this.active) throw IgrpResponseStatusException.conflict("Feriado já está activo.");
        this.active = true;
    }
}

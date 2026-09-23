package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.PublicHolidayId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;

/**
 * Feriado do calendário da instituição.
 *
 * <p><b>Recorrente</b> quer dizer «no mesmo dia e mês, todos os anos, a partir do ano da
 * data» (V55). É para os de data fixa — 1 de Janeiro, 5 de Julho, 25 de Dezembro. Os móveis
 * (Sexta-feira Santa, Corpus Christi) carregam-se ano a ano: a data depende da Páscoa, e
 * qual das festas móveis é feriado é matéria de lei, não aritmética.
 *
 * <p><b>Área</b> é um {@code ckey} de {@code AREA_GEOGRAFICA}: o feriado só conta para quem
 * trabalha numa unidade orgânica dessa área. Sem área vale para toda a gente, que é o caso
 * normal. Um feriado <b>nacional</b> é de todo o território e não pode ter área.
 */
@Getter
public class PublicHoliday {

    private PublicHolidayId id;
    private String name;
    private LocalDate holidayDate;
    private boolean national;
    private String description;
    private boolean recurring;
    private String areaCkey;
    private boolean active;

    private PublicHoliday() {}

    private PublicHoliday(PublicHolidayId id, String name, LocalDate holidayDate,
                          boolean national, String description, boolean recurring,
                          String areaCkey, boolean active) {
        this.id = id;
        this.name = name;
        this.holidayDate = holidayDate;
        this.national = national;
        this.description = description;
        this.recurring = recurring;
        this.areaCkey = areaCkey;
        this.active = active;
    }

    public static PublicHoliday criar(String name, LocalDate holidayDate, boolean national,
                                      String description, boolean recurring, String areaCkey) {
        Objects.requireNonNull(name, "name não pode ser nulo");
        Objects.requireNonNull(holidayDate, "holidayDate não pode ser nulo");
        String area = normalizarArea(areaCkey);
        validar(holidayDate, national, recurring, area);
        return new PublicHoliday(PublicHolidayId.gerarNovo(), name, holidayDate, national,
                description, recurring, area, true);
    }

    public static PublicHoliday reconstruir(PublicHolidayId id, String name, LocalDate holidayDate,
                                            boolean national, String description, boolean recurring,
                                            String areaCkey, boolean active) {
        return new PublicHoliday(id, name, holidayDate, national, description, recurring, areaCkey, active);
    }

    public void atualizar(String name, LocalDate holidayDate, boolean national, String description,
                          boolean recurring, String areaCkey) {
        String area = normalizarArea(areaCkey);
        validar(holidayDate, national, recurring, area);
        this.name = name;
        this.holidayDate = holidayDate;
        this.national = national;
        this.description = description;
        this.recurring = recurring;
        this.areaCkey = area;
    }

    public void desativar() {
        if (!this.active) throw IgrpResponseStatusException.conflict("Feriado já está inactivo.");
        this.active = false;
    }

    public void reativar() {
        if (this.active) throw IgrpResponseStatusException.conflict("Feriado já está activo.");
        this.active = true;
    }

    private static void validar(LocalDate data, boolean national, boolean recurring, String areaCkey) {
        // Nacional é todo o território: uma área nele seria uma contradição que ninguém
        // saberia ler. A base também o impede (ck_public_holiday_nacional_sem_area).
        if (national && areaCkey != null)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Um feriado nacional vale para todo o território e não tem área geográfica.");

        // Um 29 de Fevereiro «todos os anos» só aconteceria de quatro em quatro: não é
        // recorrente, é outra coisa, e não há feriado nenhum assim.
        if (recurring && data != null && data.getMonth() == Month.FEBRUARY && data.getDayOfMonth() == 29)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Um feriado a 29 de Fevereiro não pode ser recorrente.");
    }

    private static String normalizarArea(String areaCkey) {
        return areaCkey == null || areaCkey.isBlank() ? null : areaCkey.trim();
    }
}

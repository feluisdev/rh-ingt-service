package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Option {

    private ExternalID id;
    private String ccode;
    private String ckey;
    private String cvalue;
    private String locale;
    private Integer sortOrder;
    private boolean active;
    private String description;

    private Option() {}

    private Option(ExternalID id, String ccode, String ckey, String cvalue,
                   String locale, Integer sortOrder, boolean active, String description) {
        this.id = id;
        this.ccode = ccode;
        this.ckey = ckey;
        this.cvalue = cvalue;
        this.locale = locale;
        this.sortOrder = sortOrder;
        this.active = active;
        this.description = description;
    }

    public static Option criar(String ccode, String ckey, String cvalue,
                               String locale, Integer sortOrder, String description) {
        Objects.requireNonNull(ccode, "ccode não pode ser nulo");
        Objects.requireNonNull(ckey, "ckey não pode ser nulo");
        Objects.requireNonNull(cvalue, "cvalue não pode ser nulo");
        /*if (OptionCcode.fromCode(ccode).isEmpty()) {
            throw IgrpResponseStatusException.badRequest(
                "ccode inválido: '" + ccode + "'. Valores aceites: " + OptionCcode.codigosValidos());
        }*/
        String effectiveLocale = (locale == null || locale.isBlank()) ? "pt-CV" : locale;
        int effectiveSortOrder = (sortOrder == null) ? 0 : sortOrder;
        return new Option(ExternalID.gerarNovo(), ccode, ckey, cvalue,
                         effectiveLocale, effectiveSortOrder, true, description);
    }

    public static Option reconstruir(ExternalID id, String ccode, String ckey, String cvalue,
                                      String locale, Integer sortOrder, boolean active, String description) {
        return new Option(id, ccode, ckey, cvalue, locale, sortOrder, active, description);
    }

    public void atualizar(String cvalue, Integer sortOrder, String description) {
        if (cvalue != null && !cvalue.isBlank()) {
            this.cvalue = cvalue;
        }
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        this.description = description;
    }

    public void desativar() {
        if (!this.active) {
            throw IgrpResponseStatusException.conflict("Etiqueta já está inactiva.");
        }
        this.active = false;
    }

    public void reativar() {
        if (this.active) {
            throw IgrpResponseStatusException.conflict("Etiqueta já está activa.");
        }
        this.active = true;
    }

    public static String codigosValidos() {
        return OptionCcode.codigosValidos();
    }
}

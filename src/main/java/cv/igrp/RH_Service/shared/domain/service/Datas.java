package cv.igrp.RH_Service.shared.domain.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Datas como o utilizador as lê: {@code dd/MM/aaaa} (e {@code dd/MM/aaaa HH:mm}). */
public final class Datas {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DIA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Datas() {}

    public static String pt(LocalDate d) {
        return d == null ? "" : d.format(DIA);
    }

    public static String pt(LocalDateTime d) {
        return d == null ? "" : d.format(DIA_HORA);
    }

    /** «de 03/11/2026 a 14/11/2026», ou «em 03/11/2026» quando é um só dia. */
    public static String periodo(LocalDate de, LocalDate ate) {
        if (ate == null || ate.equals(de)) return "em " + pt(de);
        return "de " + pt(de) + " a " + pt(ate);
    }
}

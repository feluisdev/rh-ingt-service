package cv.igrp.RH_Service.shared.domain.service;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

/** O que chega de fora, lido com mensagens para o utilizador: ids que não existem são 404, corpo em falta é 422. */
public final class Entrada {

    private Entrada() {}

    /** Um id do caminho ou do corpo. Mal escrito é como não existir: «{recurso} não encontrado». */
    public static UUID uuid(String valor, String recurso) {
        if (valor == null || valor.isBlank())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Indique " + recurso + ".");
        try {
            return UUID.fromString(valor.trim());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.notFound(maiuscula(recurso) + " não encontrado.");
        }
    }

    /** O mesmo, mas opcional: nulo ou em branco dá nulo. */
    public static UUID uuidOpcional(String valor, String recurso) {
        return valor == null || valor.isBlank() ? null : uuid(valor, recurso);
    }

    /** O corpo do pedido, que tem de vir. */
    public static <T> T corpo(T dto, String oQueDeveTrazer) {
        if (dto == null)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "O pedido não traz dados: " + oQueDeveTrazer + ".");
        return dto;
    }

    private static String maiuscula(String s) {
        String sem = s.replaceFirst("^(o|a|os|as) ", "");
        return sem.isEmpty() ? s : Character.toUpperCase(sem.charAt(0)) + sem.substring(1);
    }
}

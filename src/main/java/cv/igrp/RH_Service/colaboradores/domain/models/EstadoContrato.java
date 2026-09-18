package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Estado de execução do contrato. Eram três cadeias de caracteres soltas
 * ({@code "ATIVO"}, {@code "SUSPENSO"}, {@code "CESSADO"}) comparadas com
 * {@code equals} por toda a aplicação; passam a ser um tipo, para que o compilador
 * apanhe um estado escrito à mão.
 *
 * <p>Não é parametrizável: são os três estados possíveis de um contrato em execução,
 * suspensão ou fim. O que varia entre instituições é o <b>motivo</b> da suspensão ou
 * da cessação, e esse é livre (catálogo {@code Option}).
 *
 * <p>Na base de dados continua a ser guardado o nome do valor, para não obrigar a
 * migrar dados nem quebrar quem lê a coluna.
 */
public enum EstadoContrato {

    /** Em execução. */
    ATIVO,

    /** Execução suspensa — o vínculo mantém-se (inactividade, art. 120.º e 121.º). */
    SUSPENSO,

    /** Terminado. Estado final: um contrato cessado não volta atrás. */
    CESSADO;

    /** Converte o valor guardado; nulo ou vazio devolve nulo. */
    public static EstadoContrato de(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return EstadoContrato.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Estado de contrato inválido: '" + valor + "'. Valores aceites: "
                            + Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", ")) + ".");
        }
    }

    /** Nome a guardar, tolerante a nulos. */
    public static String texto(EstadoContrato estado) {
        return estado == null ? null : estado.name();
    }
}

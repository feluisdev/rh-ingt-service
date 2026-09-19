package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Título com que alguém ocupa um Lugar. Eram três cadeias de caracteres soltas em
 * {@code Assignment}, comparadas com {@code equals}; passam a ser um tipo, no mesmo
 * molde do {@link EstadoContrato}.
 *
 * <p>Não é parametrizável: a lei fixa as formas de ocupar um Lugar do quadro. O que
 * varia com a instituição é o <b>motivo</b>, e esse é livre.
 *
 * <p>A distinção manda na regra "uma cadeira, um ocupante": um Lugar tem no máximo um
 * <b>titular</b> ({@link #PRINCIPAL}), mas pode ter ao mesmo tempo quem o ocupa a outro
 * título — daí o índice único do Lugar ser parcial (V45).
 *
 * <p>Na base de dados continua a ser guardado o nome do valor.
 */
public enum TipoAfectacao {

    /** Titular do Lugar. Um por Lugar, um por funcionário. */
    PRINCIPAL,

    /**
     * Substitui o titular temporariamente impedido, sem o desalojar
     * (Lei n.º 20/X/2023, art. 73.º al. a)–c) e art. 91.º n.º 1 al. a)).
     */
    SUBSTITUICAO,

    /** Exercício cumulativo de outro Lugar (art. 134.º n.º 2 al. b)). */
    ACUMULACAO;

    /** Converte o valor guardado; nulo ou vazio devolve {@link #PRINCIPAL}, que é o caso comum. */
    public static TipoAfectacao de(String valor) {
        if (valor == null || valor.isBlank()) return PRINCIPAL;
        try {
            return TipoAfectacao.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Tipo de afectação inválido: '" + valor + "'. Valores aceites: "
                            + codigosValidos() + ".");
        }
    }

    /** Nome a guardar, tolerante a nulos. */
    public static String texto(TipoAfectacao tipo) {
        return tipo == null ? null : tipo.name();
    }

    public static String codigosValidos() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }

    public boolean isPrincipal() {
        return this == PRINCIPAL;
    }
}

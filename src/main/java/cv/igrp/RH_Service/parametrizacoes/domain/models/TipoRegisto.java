package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Natureza do registo: licença ou mobilidade.
 *
 * <p>Existia um terceiro valor, {@code AMBOS}, que não corresponde a nada na lei e
 * deixava o comportamento por decidir. Os dois têm efeitos opostos no vínculo — a
 * licença suspende-o (Lei n.º 20/X/2023, art. 171.º n.º 2), enquanto na mobilidade
 * o funcionário continua a exercer funções, noutro serviço (art. 132.º a 137.º) —
 * logo um registo não pode ser as duas coisas. A V43 converte o que restava em
 * mobilidade, que era como o código já o tratava.
 */
public enum TipoRegisto {

    /** Suspende o vínculo; pode ou não libertar o Lugar, conforme o subtipo. */
    LICENCA,

    /** O funcionário exerce funções noutro serviço, mantendo o seu Lugar. */
    MOBILIDADE;

    public static TipoRegisto de(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return TipoRegisto.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Tipo de registo inválido: '" + valor + "'. Valores aceites: "
                            + Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", ")) + ".");
        }
    }
}

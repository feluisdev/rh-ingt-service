package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * O que acontece ao funcionário quando a licença termina.
 *
 * <p>Quem manteve o Lugar regressa a ele. Quem o perdeu tem direito a uma vaga
 * existente ou à primeira que ocorra (DL n.º 3/2010, art. 50.º a 53.º), e é isso
 * a disponibilidade da Lei n.º 20/X/2023, art. 122.º: aguarda vaga, com contagem
 * de tempo e abonos, e com precedência sobre transferências (n.º 4).
 */
public enum EfeitoNoRegresso {

    /** Volta ao Lugar de que nunca deixou de ser titular. */
    REGRESSA_LUGAR,

    /** Fica a aguardar vaga na sua categoria (art. 122.º). */
    DISPONIBILIDADE;

    public static EfeitoNoRegresso de(String valor) {
        if (valor == null || valor.isBlank()) return REGRESSA_LUGAR;
        try {
            return EfeitoNoRegresso.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Efeito no regresso inválido: '" + valor + "'. Valores aceites: "
                            + Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", ")) + ".");
        }
    }
}

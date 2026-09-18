package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * O que a licença faz ao Lugar do funcionário enquanto dura.
 *
 * <p>A lei distingue as licenças que mantêm o lugar das que o libertam
 * (DL n.º 3/2010, art. 46.º e 48.º contra art. 50.º a 53.º), e em alguns casos
 * faz depender a abertura da vaga de um prazo — cônjuge no estrangeiro além de
 * um ano (art. 56.º n.º 2), formação além de seis meses (art. 67.º n.º 3), o
 * mesmo que a Lei n.º 20/X/2023 diz no art. 118.º n.º 2. Por isso o prazo é um
 * número no catálogo ({@code vacancy_after_days}) e não uma constante no código.
 */
public enum EfeitoNoLugar {

    /** O funcionário continua titular do Lugar; pode ser substituído a prazo. */
    MANTEM,

    /** O Lugar fica vago e pode ser provido por outro funcionário. */
    ABRE_VAGA;

    /**
     * Abre vaga para uma licença com esta duração?
     *
     * @param duracaoEmDias duração do período; nulo significa período aberto, e um
     *                      período aberto ultrapassa qualquer prazo
     * @param aposDias      prazo a partir do qual abre vaga; nulo = abre logo
     */
    public boolean abreVaga(Long duracaoEmDias, Integer aposDias) {
        if (this != ABRE_VAGA) return false;
        if (aposDias == null) return true;
        if (duracaoEmDias == null) return true;
        return duracaoEmDias > aposDias;
    }

    public static EfeitoNoLugar de(String valor) {
        if (valor == null || valor.isBlank()) return MANTEM;
        try {
            return EfeitoNoLugar.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Efeito no Lugar inválido: '" + valor + "'. Valores aceites: "
                            + Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", ")) + ".");
        }
    }
}

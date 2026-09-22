package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Como a mobilidade é prestada — Lei n.º 20/X/2023, art. 134.º n.º 2, que classifica a
 * mobilidade geral <b>quanto à forma de prestação</b>.
 *
 * <p>Não é parametrizável: são as duas formas que a lei prevê. O que varia com a instituição é o
 * <b>motivo</b>, e esse é livre.
 *
 * <p><b>Nenhuma delas ocupa Lugar no destino</b> enquanto a mobilidade for transitória
 * (art. 135.º n.º 7), e o tempo conta no lugar de origem (art. 137.º). A diferença é o que a
 * pessoa deixa de fazer na origem: a tempo inteiro deixa de lá exercer funções, em acumulação
 * continua a exercê-las nos dois sítios.
 *
 * <p>Não confundir com a <b>acumulação de funções públicas do art. 21.º</b>, que é outro
 * instituto — regime de permissão, com incompatibilidade, manifesto interesse público e, em
 * regra, não remunerada (n.º 1); sendo remunerada, só nos casos taxativos do n.º 2. Essa não
 * está implementada.
 */
public enum FormaPrestacaoMobilidade {

    /** «Quando o funcionário passa a desempenhar funções noutro serviço, em regime de exclusividade.» */
    TEMPO_INTEIRO,

    /**
     * «Quando o funcionário passa a exercer funções noutro serviço, em acumulação com as do
     * serviço de origem.»
     */
    ACUMULACAO;

    /** Nulo ou vazio vale {@link #TEMPO_INTEIRO}: a exclusividade é a regra (art. 20.º). */
    public static FormaPrestacaoMobilidade de(String valor) {
        if (valor == null || valor.isBlank()) return TEMPO_INTEIRO;
        try {
            return FormaPrestacaoMobilidade.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Forma de prestação inválida: '" + valor + "'. Valores aceites: "
                            + codigosValidos() + ".");
        }
    }

    public static String texto(FormaPrestacaoMobilidade forma) {
        return forma == null ? null : forma.name();
    }

    public static String codigosValidos() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }

    public boolean isAcumulacao() {
        return this == ACUMULACAO;
    }
}

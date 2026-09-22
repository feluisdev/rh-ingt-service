package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * A escolha do art. 43.º n.º 2 do DL n.º 3/2010: as faltas injustificadas «implicam a
 * <b>opção</b> entre a perda das remunerações correspondentes aos dias de ausência, ou o seu
 * desconto nas férias».
 *
 * <p><b>É a única escolha que este artigo dá.</b> O desconto na antiguidade, no mesmo número, é
 * imperativo — não se pergunta a ninguém se quer descontar. Já entre perder a remuneração e
 * descontar nas férias, a lei deixa escolher; e a escolha é de <b>cada caso</b>, não do catálogo.
 * Por isso vive no pedido: duas faltas injustificadas da mesma pessoa podem ser resolvidas de
 * maneiras diferentes.
 *
 * <p>Nenhuma das duas é executada por esta aplicação. A perda de remuneração é do sistema que
 * processa vencimentos; o desconto nas férias, quando for implementado, sai do saldo do ano. O
 * que aqui se faz é <b>registar o que foi decidido</b>, sem o qual ninguém sabe o que decidir
 * depois.
 */
public enum OpcaoFaltaInjustificada {

    /** Perde-se a remuneração dos dias de ausência. Fica para o sistema de vencimentos. */
    PERDA_REMUNERACAO,

    /** Os dias são descontados nas férias, em vez de na remuneração. */
    DESCONTO_FERIAS;

    public static OpcaoFaltaInjustificada de(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return OpcaoFaltaInjustificada.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Opção de falta injustificada inválida: '" + valor + "'. Valores possíveis: "
                            + "PERDA_REMUNERACAO, DESCONTO_FERIAS (art. 43.º n.º 2).");
        }
    }
}

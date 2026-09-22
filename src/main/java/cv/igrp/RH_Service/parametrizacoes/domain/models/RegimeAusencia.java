package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * O <b>regime legal</b> a que um tipo de ausência obedece — o que a lei fixa, por oposição ao
 * motivo, que a instituição parametriza.
 *
 * <p>O DL n.º 3/2010 trata férias e faltas em <b>capítulos distintos</b>, com regras que não se
 * misturam: as férias <b>vencem-se</b> a 1 de Janeiro num número de dias fixado por lei
 * (art. 2.º n.os 3 e 4) e são irrenunciáveis (n.º 5); uma falta não se vence — acontece, e o que
 * a lei lhe associa é justificação e efeito na remuneração (art. 16.º).
 *
 * <p><b>Porque é que isto não se lê do código do catálogo.</b> O catálogo de tipos de ausência é
 * da instituição: os códigos, os nomes e os motivos são dela, e o projecto já decidiu não os
 * validar. Mas o <i>regime</i> não é dela — é da lei. Sem esta classificação, saber quais das
 * linhas do catálogo são férias obrigaria a procurar o código {@code 'FERIAS'} escrito no código
 * Java, que é exactamente o que se evitou na substituição (lida da situação funcional) e no
 * efeito da licença no Lugar (lido do subtipo).
 *
 * <p>É o mesmo padrão de {@link SituacaoFuncional} e de {@code position_effect}: a instituição
 * <b>mapeia</b> as suas linhas nos regimes que a lei conhece, sem lhes poder inventar um novo.
 */
public enum RegimeAusencia {

    /**
     * Férias (cap. II). Vencem-se anualmente, em dias úteis, e o saldo nasce sozinho — não é
     * criado à mão. No ano de ingresso o direito é proporcional ao tempo de serviço (art. 3.º).
     */
    FERIAS,

    /**
     * Faltas e restantes ausências curtas (cap. III). Não se vencem: o saldo, quando existe,
     * é um tecto do próprio tipo ({@code max_days_per_year}) e é configurado pela instituição.
     */
    FALTA;

    /**
     * Lê um regime vindo de fora. Nulo ou em branco devolve {@code null} — quem chama decide o
     * que isso quer dizer: na criação vale {@link #FALTA}, na actualização mantém-se o que está.
     *
     * <p>Um valor desconhecido é <b>recusado</b> com 422, e não tratado como omissão: a lista é
     * fechada porque os regimes são os da lei, e aceitar em silêncio um {@code "FÉRIAS"} mal
     * escrito deixaria o tipo classificado como falta sem ninguém dar por isso.
     */
    public static RegimeAusencia de(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return RegimeAusencia.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Regime de ausência inválido: '" + valor + "'. Valores possíveis: FERIAS, FALTA.");
        }
    }
}

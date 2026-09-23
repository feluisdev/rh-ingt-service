package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Como se contam os dias de uma ausência — DL n.º 3/2010, art. 76.º (V56).
 *
 * <p>«Os dias de descanso semanal ou complementar e os feriados, quando intercalados no decurso
 * de uma licença ou de uma sucessão de faltas da mesma natureza, integram-se no cômputo dos
 * respectivos períodos de duração, <b>salvo se a lei se referir expressamente a dias úteis</b>.»
 *
 * <p>A regra é {@link #DIAS_SEGUIDOS}; {@link #DIAS_UTEIS} é a excepção, e tem de estar escrita
 * na disposição — as férias (art. 2.º n.º 3), os dois direitos do trabalhador-estudante
 * (art. 77.º n.os 2 e 3). Como em {@link RegimeAusencia}: a instituição diz a que disposição
 * corresponde cada linha do seu catálogo; o modo de contar é da lei.
 */
public enum ContagemDias {

    /** Seg-Sex, sem os feriados que se aplicam ao colaborador. */
    DIAS_UTEIS,

    /**
     * Dias de calendário entre o primeiro e o último dia útil do período. Os fins-de-semana e
     * feriados <b>intercalados</b> contam; os das pontas não, porque não estão «no decurso» da
     * ausência — quem falta de sexta a domingo faltou um dia.
     */
    DIAS_SEGUIDOS;

    /** Nulo ou em branco: nulo, e quem chama decide (criar: DIAS_UTEIS; actualizar: mantém). */
    public static ContagemDias de(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return ContagemDias.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Contagem de dias inválida: '" + valor + "'. Valores possíveis: DIAS_UTEIS, DIAS_SEGUIDOS.");
        }
    }
}

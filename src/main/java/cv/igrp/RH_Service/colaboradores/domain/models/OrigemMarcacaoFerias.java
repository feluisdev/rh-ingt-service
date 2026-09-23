package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Como se chegou à marcação das férias — DL n.º 3/2010, art. 5.º.
 *
 * <p>Não há um terceiro valor, e não há «aprovação»: o art. 6.º n.º 1 manda o serviço
 * <i>elaborar</i> o mapa e <i>dar conhecimento</i>. Ou houve acordo (n.º 3), ou o dirigente
 * fixou (n.º 5).
 */
public enum OrigemMarcacaoFerias {

    /** Marcadas «de acordo com os interesses das partes» (n.º 3). */
    ACORDO,

    /**
     * «Na falta de acordo, as férias são fixadas pelo dirigente competente para o período entre 1
     * de Maio e 31 de Outubro» (n.º 5). É também o caso de quem não indicou preferência: sem
     * preferência não há acordo.
     */
    FIXADA;

    public static OrigemMarcacaoFerias de(String valor) {
        if (valor == null || valor.isBlank())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A marcação tem de dizer a origem: ACORDO (art. 5.º n.º 3) ou FIXADA pelo dirigente (n.º 5).");
        try {
            return OrigemMarcacaoFerias.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Origem da marcação inválida: '" + valor + "'. Valores possíveis: ACORDO, FIXADA.");
        }
    }
}

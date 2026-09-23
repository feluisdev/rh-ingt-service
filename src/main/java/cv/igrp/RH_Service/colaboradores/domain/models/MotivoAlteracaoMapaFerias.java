package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Porque se altera uma marcação depois de o mapa ter sido dado a conhecer — DL n.º 3/2010,
 * art. 6.º n.º 2: «Salvos os casos resultantes de conveniência de serviço, devidamente
 * fundamentada, o mapa de férias só pode ser alterado posteriormente a 31 de Março por acordo
 * entre os serviços e os interessados.» São as duas saídas que a lei dá, e só essas.
 */
public enum MotivoAlteracaoMapaFerias {

    /** Por acordo entre o serviço e o interessado. */
    ACORDO,

    /** Por conveniência de serviço — e então tem de vir <b>fundamentada</b>. */
    CONVENIENCIA_SERVICO;

    public static MotivoAlteracaoMapaFerias de(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return MotivoAlteracaoMapaFerias.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Motivo de alteração inválido: '" + valor + "'. Valores possíveis: ACORDO, CONVENIENCIA_SERVICO.");
        }
    }
}

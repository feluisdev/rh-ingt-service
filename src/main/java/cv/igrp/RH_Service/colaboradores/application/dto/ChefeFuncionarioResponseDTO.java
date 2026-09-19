package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Chefe directo do colaborador, pelo reporte estrutural do Lugar.
 *
 * <p>O {@code estado} distingue três situações que o ecrã tem de mostrar de maneira
 * diferente: PROVIDO, CHEFIA_VAGA (o Lugar de chefia existe mas não tem titular) e
 * SEM_CHEFIA_DEFINIDA (o Lugar não reporta a nenhum).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ChefeFuncionarioResponseDTO {

    private String funcionarioId;

    private String funcionarioNome;

    private String chefePositionId;

    private String chefeFuncionarioId;

    private String chefeNome;

    /** PROVIDO · CHEFIA_VAGA · SEM_CHEFIA_DEFINIDA. */
    private String estado;
}

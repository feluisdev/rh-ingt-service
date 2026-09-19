package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resultado do registo de um funcionário só com dados pessoais.
 *
 * <p>Tem DTO próprio por causa do <b>número de funcionário</b>, que é atribuído pela
 * aplicação e o ecrã tem de mostrar: não se deduz de nada que o cliente já tenha.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class FuncionarioCriadoResponseDTO {

    /** Id do funcionário criado. */
    private String id;

    /** Número atribuído pela aplicação. */
    private String numeroFuncionario;
}

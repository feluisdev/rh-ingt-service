package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resultado de uma transição de licença/mobilidade (aprovar, activar, encerrar).
 *
 * <p>Tem DTO próprio porque a transição pode produzir <b>efeitos no Lugar e no estado</b>
 * do trabalhador, e o ecrã precisa de os saber: uma licença que abre vaga encerra a
 * afectação, e o regresso atribui um estado novo. Quando não há efeito, os campos vêm
 * nulos — é a resposta a dizer que nada disso aconteceu.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class LicencaEfeitoResponseDTO {

    /** Id da licença/mobilidade. */
    private String id;

    /** Estado em que o registo ficou. */
    private String estado;

    /** A transição produziu efeito; falso quando já estava nesse estado. */
    private boolean sucesso;

    /** Afectação encerrada por a licença abrir vaga, se alguma. */
    private String afectacaoEncerradaId;

    /** Estado do trabalhador atribuído pela transição, se algum. */
    private String estadoAtribuidoId;
}

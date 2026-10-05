package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dotação e vagas de uma unidade.
 *
 * <p>{@code ocupados} conta <b>titulares</b>, não ocupantes: um Lugar cujo titular está
 * impedido e tem substituto continua a ser do titular, e um Lugar com substituto mas sem
 * titular conta como vago (V45).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class VagasUnidadeResponseDTO {

    private String unidadeId;

    /** Lugares activos da unidade. */
    private long dotacao;

    /** Lugares com titular. */
    private long ocupados;

    /** dotacao - ocupados. */
    private long vagas;

    /** Das vagas, quantas estão reservadas para quem aguarda o contrato (BR-AF-23). */
    private long reservados;
}

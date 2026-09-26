package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Abrir, acrescentar um item ou cancelar. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ChecklistRequestDTO {
    /** Abrir: ENTRADA, SAIDA. */
    private String tipo;
    /** Abrir: a data da entrada ou da saída (por omissão, hoje). */
    private LocalDate dataReferencia;
    /** Acrescentar. */
    private String descricao;
    /** Acrescentar. */
    private String responsavel;
    /** Acrescentar. */
    private Boolean obrigatorio;
    /** Acrescentar. */
    private LocalDate prazo;
    /** Cancelar. */
    private String motivo;
}

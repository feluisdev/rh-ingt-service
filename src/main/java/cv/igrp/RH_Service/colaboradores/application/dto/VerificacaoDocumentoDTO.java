package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** A verificação pública de um documento pelo código impresso. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class VerificacaoDocumentoDTO {
    private String codigo;
    /** Existe e não foi anulado. */
    private boolean valido;
    private boolean anulado;
    private String tipo;
    private String numero;
    private String titulo;
    private LocalDateTime emitidoEm;
    private String titular;
}

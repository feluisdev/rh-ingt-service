package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Um documento emitido pelo RH (o PDF está no MinIO; descarrega-se pelo link). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class DocumentoEmitidoDTO {
    private String id;
    /** DECLARACAO, CARTAO_PROFISSIONAL, EXTRACTO_PUBLICACAO */
    private String tipo;
    /** Ex.: DEC-2026-000001 */
    private String numero;
    private String funcionarioId;
    private String titulo;
    private LocalDateTime emitidoEm;
    private String codigoVerificacao;
    /** SHA-256 do PDF. */
    private String impressaoDigital;
    private boolean anulado;
    private LocalDateTime anuladoEm;
    private String motivoAnulacao;
}

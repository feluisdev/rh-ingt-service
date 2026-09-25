package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Um cartão de identificação profissional e se vale hoje. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class CartaoProfissionalDTO {
    private String id;
    private String funcionarioId;
    /** O PDF (documento emitido no MinIO). */
    private String documentoId;
    private String numero;
    private LocalDate emitidoEm;
    private String categoria;
    private String funcao;
    private String cargo;
    /** EMITIDO, ENTREGUE, DEVOLVIDO, ANULADO */
    private String estado;
    private LocalDate dataEntrega;
    private LocalDate dataDevolucao;
    private String motivoAnulacao;
    private boolean valido;
    /** Porque já não vale (mudou de categoria, de função, saiu, foi devolvido...). */
    private String motivoInvalidade;
}

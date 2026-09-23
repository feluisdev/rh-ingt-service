package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Uma picagem de relógio. De quem é: {@code numeroFuncionario} (o que o relógio conhece) ou {@code funcionarioId}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PicagemDTO {
    private String numeroFuncionario;
    private String funcionarioId;
    private LocalDateTime momento;
    private String sentido;
    /** O identificador da picagem no relógio: obrigatório, é o que torna a importação repetível. */
    private String referenciaExterna;
}

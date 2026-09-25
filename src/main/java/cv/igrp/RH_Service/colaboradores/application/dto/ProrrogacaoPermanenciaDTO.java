package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Uma prorrogação de permanência para além dos 65 anos. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ProrrogacaoPermanenciaDTO {
    private String id;
    private String funcionarioId;
    /** PEDIDA, AUTORIZADA, INDEFERIDA */
    private String estado;
    private LocalDate dataPedido;
    private String propostaFundamentada;
    private LocalDate validaAte;
    private String despachoNumero;
    private LocalDate despachoData;
    private String motivoIndeferimento;
}

package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Uma comissão de serviço (registo de mobilidade do subtipo que regressa ou cessa). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ComissaoServicoDTO {
    private String licencaId;
    private String funcionarioId;
    private String funcionarioNome;
    private String subtipo;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private int renovacoes;
    /** POR_INICIAR, EM_CURSO, TERMINADA */
    private String estadoPeriodo;
    /** O Lugar de direcção, se interno. */
    private String lugarDestinoId;
    private String unidadeDestinoId;
    private String entidadeDestino;
    private String despachoNumero;
    /** Traz a nota da cessação (iniciativa, aviso, efeitos). */
    private String observacoes;
    private Integer diasParaTermo;
}

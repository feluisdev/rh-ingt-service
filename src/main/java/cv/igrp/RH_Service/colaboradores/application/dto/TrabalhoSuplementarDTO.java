package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Um trabalho suplementar, com o tipo de dia e as horas realizadas (calculados das marcações). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class TrabalhoSuplementarDTO {
    private String id;
    private String funcionarioId;
    private String numeroFuncionario;
    private String funcionarioNome;
    private LocalDate data;
    private String horaInicio;
    private String horaFim;
    private String motivo;
    /** PEDIDO, AUTORIZADO, RECUSADO ou CANCELADO. */
    private String estado;
    private boolean pedidoPeloProprio;
    /** Autorizado depois do dia em que foi feito (o caso urgente). */
    private boolean autorizacaoPosterior;
    /** A chefia directa que decidiu; nulo quando foi o RH (ou ainda não foi decidido). */
    private String decididoPor;
    private LocalDateTime decididoEm;
    private String motivoRecusa;
    private String motivoCancelamento;
    /** DIA_UTIL, DESCANSO ou FERIADO; nulo sem horário. */
    private String tipoDia;
    private int minutosAutorizados;
    /** Presença (marcações válidas) dentro do intervalo; só nos autorizados. */
    private int minutosRealizados;
    /** Autorizado, num dia passado, sem nenhuma marcação válida. */
    private boolean semRegisto;
}

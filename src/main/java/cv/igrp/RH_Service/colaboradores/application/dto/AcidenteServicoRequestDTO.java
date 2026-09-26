package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Os dados de um passo do acidente (cada acção usa os seus). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AcidenteServicoRequestDTO {
    /** Participar. */
    private String tipo;
    /** Participar. */
    private LocalDateTime dataHora;
    private String local;
    private String descricao;
    private String testemunhas;
    private LocalDate dataParticipacao;
    /** Qualificar: é (ou não) acidente em serviço. */
    private Boolean emServico;
    /** Qualificar. */
    private String despacho;
    /** Qualificar (não qualificado). */
    private String motivo;
    /** Incapacidade: TEMPORARIA_ABSOLUTA, TEMPORARIA_PARCIAL. */
    private String tipoIncapacidade;
    /** Incapacidade. */
    private LocalDate inicio;
    /** Incapacidade. */
    private LocalDate fim;
    /** Alta; participação à seguradora. */
    private LocalDate data;
    /** Incapacidade permanente. */
    private BigDecimal percentagem;
    /** Incapacidade permanente. */
    private Boolean absoluta;
    /** Incapacidade permanente parcial que não deixa exercer as funções (art. 189.º n.º 4). */
    private Boolean impedeFuncoes;
    private String seguradora;
    private String apolice;
}

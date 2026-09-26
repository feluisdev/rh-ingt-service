package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Os dados de um acto do processo (cada acto usa os seus). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AccaoDisciplinarRequestDTO {
    /** Participar: por omissão, PD/AAAA/NNN. */
    private String numero;
    /** Participar. */
    private String especie;
    /** Participar. */
    private LocalDate dataInfraccao;
    /** Participar. */
    private String factos;
    /** Participar: a pena que a infracção pode merecer (para a prescrição e a suspensão preventiva). */
    private String penaPrevista;
    /** A data do acto (por omissão, hoje). */
    private LocalDate data;
    /** Instaurar. */
    private String despacho;
    /** Instaurar, decidir. */
    private String entidade;
    /** Instaurar, nomear o instrutor. */
    private String instrutorId;
    /** Instaurar, nomear o instrutor (quem não é da casa). */
    private String instrutorNome;
    /** Prorrogar a instrução, suspensão preventiva, prazo de defesa. */
    private Integer dias;
    /** Suspensão preventiva. */
    private Boolean perdaVencimento;
    /** Notificar a acusação: prazo de defesa até 45 dias. */
    private Boolean complexo;
    /** Acusar (a aplicável), relatório (a proposta; vazia = arquivar), decidir (vazia = arquivar), decidir o recurso (diminuída). */
    private String pena;
    /** Multa e suspensão em dias, inactividade em meses. */
    private Integer duracao;
    /** Acusação, defesa, relatório, fundamentação da decisão, recurso. */
    private String texto;
    /** Decidir o recurso: MANTIDA, DIMINUIDA, ANULADA. */
    private String resultado;
    /** Arquivar. */
    private String motivo;
}

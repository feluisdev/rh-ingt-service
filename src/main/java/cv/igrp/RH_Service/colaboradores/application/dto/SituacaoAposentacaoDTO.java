package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** A situação de um colaborador perante a aposentação, hoje (as datas de serviço são previsões). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class SituacaoAposentacaoDTO {
    private String funcionarioId;
    private String numeroFuncionario;
    private String nome;
    private LocalDate dataNascimento;
    private int idade;
    /** Limite de idade (art. 48.º n.º 1). */
    private LocalDate faz65;
    /** Idade máxima (art. 48.º n.º 2). */
    private LocalDate faz70;
    /** Fim da prorrogação autorizada, se houver. */
    private LocalDate prorrogadoAte;
    /** O dia em que o vínculo cessa por idade: o fim da prorrogação ou os 65. */
    private LocalDate limiteEfectivo;
    /** Tempo de serviço contado hoje, em dias (já com os descontos). */
    private long diasServico;
    private int anosServico;
    private int mesesServico;
    private int diasServicoResto;
    /** Quando reúne os 34 anos da antecipada a pedido (art. 175.º). */
    private LocalDate completa34Anos;
    /** Quando reúne 58 anos de idade e 30 de serviço (art. 179.º). */
    private LocalDate preAposentacaoPossivel;
    private boolean podeAntecipada;
    private boolean podePreAposentacao;
    private ProcessoAposentacaoDTO processoEmCurso;
    private List<ProcessoAposentacaoDTO> processos = new ArrayList<>();
    private List<ProrrogacaoPermanenciaDTO> prorrogacoes = new ArrayList<>();
    private List<String> alertas = new ArrayList<>();
}

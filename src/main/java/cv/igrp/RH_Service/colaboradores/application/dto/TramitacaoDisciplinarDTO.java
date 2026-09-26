package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** O processo disciplinar com a tramitação: fase, actos, prazos e alertas. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class TramitacaoDisciplinarDTO {
    private String id;
    private String funcionarioId;
    private String funcionarioNome;
    private String numero;
    /** DISCIPLINAR_COMUM, INFRACCAO_CONSTATADA, FALTA_ASSIDUIDADE, ABANDONO_LUGAR, INQUERITO, SINDICANCIA, AVERIGUACOES */
    private String especie;
    /** PARTICIPADO, INSTAURADO, EM_INSTRUCAO, ACUSADO, RELATORIO, DECIDIDO, NOTIFICADO, EM_RECURSO, CONCLUIDO, ARQUIVADO; nula num registo antigo. */
    private String fase;
    private LocalDate dataInfraccao;
    private String penaPrevista;
    /** A prescrição do procedimento pela pena prevista (art. 6.º). */
    private LocalDate prescreveEm;
    private String instrutorId;
    private String instrutorNome;
    /** CENSURA_ESCRITA, MULTA, SUSPENSAO, INACTIVIDADE, APOSENTACAO_COMPULSIVA, DEMISSAO, CESSACAO_COMISSAO */
    private String pena;
    /** Dias de multa ou de suspensão; meses de inactividade. */
    private Integer penaDuracao;
    private String penaDescricao;
    private LocalDate penaInicio;
    private LocalDate penaFim;
    /** Quando a pena se executa (ou executou). */
    private LocalDate dataExecucao;
    private boolean executada;
    private String factos;
    private List<ActoDisciplinarDTO> actos = new ArrayList<>();
    private List<PrazoDisciplinarDTO> prazos = new ArrayList<>();
    private List<String> alertas = new ArrayList<>();
}

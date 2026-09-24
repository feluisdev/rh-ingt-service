package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/** Um funcionário na lista de antiguidade (art. 69.º n.º 2 do DL n.º 3/2010). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class LinhaAntiguidadeDTO {
    /** Posição no cargo (1 = o mais antigo). */
    private int posicao;
    private String funcionarioId;
    private String numeroFuncionario;
    private String nome;
    private String unidadeCodigo;
    private String unidadeNome;
    private String escalao;
    /** a) Data do início de funções no cargo (categoria). */
    private LocalDate dataInicioNoCargo;
    /** b) Dias descontados nos termos da lei, no cargo. */
    private long diasDescontados;
    private long diasContados;
    /** c) Tempo contado no cargo, em anos, meses e dias (365/30). */
    private int anos;
    private int meses;
    private int dias;
    private LocalDate dataAdmissao;
    /** Tempo de serviço total desde a admissão (critério de desempate); nulo sem data de admissão. */
    private Integer anosServico;
    private Integer mesesServico;
    private Integer diasServico;
    /** Os períodos descontados, com o motivo (art. 69.º n.º 3). */
    private String observacoes;
}

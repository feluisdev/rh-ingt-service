package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Um item do modelo das checklists de entrada e saída. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ItemChecklistModeloDTO {
    private String id;
    /** ENTRADA, SAIDA */
    private String tipo;
    /** Os itens que o sistema marca sozinho (PROVIMENTO, CARTAO_PROFISSIONAL, DEVOLUCAO_CARTAO); os outros, livres. */
    private String codigo;
    private String descricao;
    /** RH, CHEFIA, PROPRIO, INFORMATICA, PATRIMONIO */
    private String responsavel;
    private boolean obrigatorio;
    /** Dias a contar da data de referência (a entrada, a saída); negativo = antes. */
    private Integer prazoDias;
    private int ordem;
    private boolean activo;
}

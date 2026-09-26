package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Criar ou alterar um item do modelo (o tipo e o código só na criação). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ItemChecklistModeloRequestDTO {
    /** ENTRADA, SAIDA */
    private String tipo;
    private String codigo;
    private String descricao;
    /** RH, CHEFIA, PROPRIO, INFORMATICA, PATRIMONIO */
    private String responsavel;
    private Boolean obrigatorio;
    private Integer prazoDias;
    private Integer ordem;
    /** Desactivar: deixa de entrar nas checklists novas. */
    private Boolean activo;
}

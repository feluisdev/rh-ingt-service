package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Um item de uma checklist. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ItemChecklistDTO {
    private String id;
    private String codigo;
    private String descricao;
    /** RH, CHEFIA, PROPRIO, INFORMATICA, PATRIMONIO */
    private String responsavel;
    private boolean obrigatorio;
    private LocalDate prazo;
    /** PENDENTE, FEITO, NAO_APLICAVEL */
    private String estado;
    /** Quando ficou feito ou não aplicável. */
    private LocalDate data;
    private String observacao;
    /** Marcado pelo sistema (o cartão entregue, o provimento registado...). */
    private boolean automatico;
    /** Pendente e com o prazo já passado. */
    private boolean atrasado;
}

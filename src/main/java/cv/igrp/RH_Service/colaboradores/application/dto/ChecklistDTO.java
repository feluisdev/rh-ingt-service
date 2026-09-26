package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** A checklist de entrada ou de saída de um colaborador. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ChecklistDTO {
    private String id;
    private String funcionarioId;
    private String funcionarioNome;
    /** ENTRADA, SAIDA */
    private String tipo;
    /** A data da entrada ou da saída. */
    private LocalDate dataReferencia;
    /** ABERTA, CONCLUIDA, CANCELADA */
    private String estado;
    private LocalDate abertaEm;
    private LocalDate concluidaEm;
    private String motivoCancelamento;
    private int pendentes;
    private int atrasados;
    private List<ItemChecklistDTO> itens = new ArrayList<>();
}

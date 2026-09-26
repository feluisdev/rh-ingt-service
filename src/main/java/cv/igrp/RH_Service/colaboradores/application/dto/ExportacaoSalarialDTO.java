package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * O que o salarial recebe de um mês (contrato versionado): os factos pelo mês de competência (com o bruto base do escalão
 * nos movimentos) e a relação mensal — congelada, se o mês está fechado; a de hoje, provisória, se não.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ExportacaoSalarialDTO {
    private int versao;
    private String mes;
    /** ABERTO, FECHADO, REABERTO. */
    private String estado;
    private LocalDateTime fechadoEm;
    /** A relação é a de hoje, não a do fecho. */
    private boolean provisoria;
    private int totalFactos;
    private List<FactoRhDTO> factos = new ArrayList<>();
    /** Uma linha por colaborador: identificação (número, NIF), dias, faltas por natureza, licenças, trabalho suplementar. */
    private List<Map<String, Object>> relacao = new ArrayList<>();
}

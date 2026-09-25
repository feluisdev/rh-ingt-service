package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Quem atinge o limite de idade, os 34 anos de serviço ou as condições da pré-aposentação até uma data. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class RelatorioAposentacaoDTO {
    private LocalDate dataReferencia;
    private LocalDate ate;
    private String unidadeId;
    private int total;
    private List<SituacaoAposentacaoDTO> linhas = new ArrayList<>();
}

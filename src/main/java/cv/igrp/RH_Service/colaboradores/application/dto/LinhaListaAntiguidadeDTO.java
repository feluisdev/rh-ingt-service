package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Uma linha congelada da lista de antiguidade (art. 69.º n.º 2). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class LinhaListaAntiguidadeDTO {
    private int grupo;
    private String carreira;
    private String categoria;
    private boolean foraDeGrelha;
    private int posicao;
    private String funcionarioId;
    private String numeroFuncionario;
    private String nome;
    private String unidadeId;
    private String escalao;
    private LocalDate inicioNoCargo;
    private long diasDescontados;
    private long diasNoCargo;
    private int anosNoCargo;
    private int mesesNoCargo;
    private int diasNoCargoResto;
    private long diasTotais;
    private int anosTotal;
    private int mesesTotal;
    private int diasTotalResto;
}

package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Um pedido de exoneração voluntária, com as condicionantes de hoje. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ExoneracaoDTO {
    private String id;
    private String funcionarioId;
    private String funcionarioNome;
    private LocalDate dataPreAviso;
    /** Pelo menos 60 dias depois do pré-aviso. */
    private LocalDate dataPretendida;
    /** Pré-aviso + 90 dias: o máximo que a condição adia. */
    private LocalDate dataLimite;
    private String motivo;
    private boolean pedidaPeloProprio;
    /** PEDIDA, DEFERIDA, EFECTIVADA, DESISTIDA */
    private String estado;
    private String despacho;
    private LocalDate dataDespacho;
    /** Quando produziu efeitos. */
    private LocalDate dataEfeito;
    /** Com o que hoje se sabe. */
    private LocalDate dataEfeitoPrevista;
    /** Processo disciplinar, inquérito ou sindicância, garantia de formação (art. 95.º). */
    private List<String> condicionantes = new ArrayList<>();
}

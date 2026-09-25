package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Os dados de cada passo do processo: deferir, indeferir, desligar, concluir, cancelar. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class DecisaoAposentacaoRequestDTO {
    /** Deferir. */
    private String despachoNumero;
    /** Deferir. */
    private LocalDate despachoData;
    /** Deferir (opcional). */
    private LocalDate dataPrevista;
    /** Indeferir e cancelar. */
    private String motivo;
    /** Desligar (data da desligação) e concluir (data da aposentação). */
    private LocalDate data;
    /** Desligar na pré-aposentação (70 a 80). */
    private BigDecimal percentagemPrestacao;
    /** Desligar e concluir: o estado a atribuir (opcional; por omissão o do catálogo pela situação). */
    private String workerStateId;
    private String observacao;
}

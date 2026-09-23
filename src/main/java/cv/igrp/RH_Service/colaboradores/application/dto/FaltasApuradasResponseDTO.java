package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class FaltasApuradasResponseDTO {
    private String funcionarioId;
    /** yyyy-MM */
    private String mes;
    /** Isenção de horário no contrato: sem débito a apurar. */
    private boolean isento;
    private List<DiaApuradoDTO> dias = new ArrayList<>();
    private List<DebitoAfericaoDTO> debitos = new ArrayList<>();
    /** Dias de trabalho sem nenhuma marcação: faltas de dia inteiro. */
    private int diasSemRegisto;
    /** Tempo parcial do mês (incompletos, plataformas, débitos), somado (art. 13.º n.º 4). */
    private int minutosParciais;
    /** O período normal diário com que se converte o tempo parcial: a média do esperado no mês. */
    private int periodoNormalMinutos;
    /** O tempo parcial em faltas, em dias e meios-dias. */
    private BigDecimal faltasParciais;
    private BigDecimal totalFaltas;
    /** Dias com anomalias nas marcações: não se apuram sem as corrigir. */
    private int diasPorCorrigir;
    /** Dias com pedidos de correcção por decidir: não se apuram até a chefia ou o RH decidirem. */
    private int diasPorValidar;
}

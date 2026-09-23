package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/** Um colaborador na relação mensal (art. 75.º do DL n.º 3/2010). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class RelacaoMensalLinhaDTO {
    private String funcionarioId;
    private String numeroFuncionario;
    private String nome;
    /** Isenção de horário: sem apuramento de faltas por débito. */
    private boolean isento;
    /** Dias do mês antes da admissão ou depois do fim do vínculo. */
    private int diasForaDoVinculo;
    private int diasFerias;
    private List<RubricaFaltaDTO> faltasJustificadas;
    /** Faltas injustificadas já registadas (pedido FALTA_INJUSTIFICADA). */
    private List<RubricaFaltaDTO> faltasInjustificadas;
    /** Do apuramento: dias de trabalho sem nenhuma marcação. */
    private int diasSemRegisto;
    /** Do apuramento: tempo parcial do mês convertido (art. 13.º n.º 4). */
    private BigDecimal faltasParciais;
    /** Do apuramento: diasSemRegisto + faltasParciais. */
    private BigDecimal faltasPorJustificar;
    private List<RubricaLicencaDTO> licencas;
    private int minutosSuplementarDiaUtil;
    private int minutosSuplementarDescanso;
    private int minutosSuplementarFeriado;
    private int diasPorCorrigir;
    private int diasPorValidar;
    /** Pedidos de ausência por decidir com dias no mês. */
    private int pedidosPendentes;
    /** COMPLETA ou COM_PENDENCIAS. */
    private String estado;
}

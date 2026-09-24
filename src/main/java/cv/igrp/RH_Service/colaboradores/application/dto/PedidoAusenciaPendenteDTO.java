package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Um pedido de ausência por decidir, na caixa da chefia. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PedidoAusenciaPendenteDTO {
    private String id;
    private String funcionarioId;
    private String numeroFuncionario;
    private String funcionarioNome;
    private String tipoCodigo;
    private String tipoNome;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private int numeroDias;
    /** HH:mm, nos pedidos em horas. */
    private String horaInicio;
    private String horaFim;
    private int minutosPorDia;
    private String motivo;
}

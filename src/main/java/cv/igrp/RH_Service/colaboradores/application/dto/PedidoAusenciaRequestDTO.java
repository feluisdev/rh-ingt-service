package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PedidoAusenciaRequestDTO {
    @NotBlank
    private String tipoAusenciaId;
    @NotNull
    private LocalDate dataInicio;
    @NotNull
    private LocalDate dataFim;
    private String motivo;
    /**
     * Art. 43.o n.o 2, so para tipos classificados como falta injustificada: {@code
     * PERDA_REMUNERACAO} ou {@code DESCONTO_FERIAS}. E <b>obrigatoria</b> nesses tipos e
     * <b>recusada</b> nos outros -- a lei so da a opcao a quem falta sem justificacao.
     */
    private String opcaoFaltaInjustificada;
    /**
     * V58, opcionais: pedido em horas ({@code HH:mm}). As duas ou nenhuma; valem em cada dia do
     * intervalo. Sem elas o pedido e de dias inteiros, como sempre foi.
     */
    private String horaInicio;
    private String horaFim;
}

package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * O que a suspensão produziu. Quem interrompe férias quer saber, na hora, quantos dias
 * recuperou e com quantos fica.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuspensaoFeriasResponseDTO {

    private String pedidoId;

    /** Data a partir da qual as férias deixaram de correr. */
    private LocalDate suspensoEm;

    /** Novo último dia de férias — a véspera da suspensão. */
    private LocalDate dataFim;

    /** Dias úteis efectivamente gozados até à interrupção. */
    private int diasGozados;

    /** Dias úteis que voltaram ao saldo. */
    private int diasRecuperados;

    /** O que passa a estar disponível no saldo do ano. */
    private Integer saldoDisponivel;
}

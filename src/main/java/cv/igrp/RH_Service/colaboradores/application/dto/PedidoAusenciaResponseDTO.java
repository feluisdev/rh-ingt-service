package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PedidoAusenciaResponseDTO {
    private String id;
    private String funcionarioId;
    private TipoAusenciaResponseDTO tipoAusencia;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private int numeroDias;
    private String motivo;
    /** Art. 43.o n.o 2: a opcao tomada, quando o tipo e falta injustificada. Nula nos outros. */
    private String opcaoFaltaInjustificada;
    private String estado;
    private String aprovadoPor;
    private LocalDate dataDecisao;
    private String observacoesDecisao;
    /** Aprovado pelo sistema: o tipo não requer aprovação (sem decisor). */
    private boolean aprovacaoAutomatica;
    private Boolean isActive;
    private String estadoDesc;
    /** Data a partir da qual as ferias deixaram de correr (art. 8.o). Nulo se nao houve suspensao. */
    private LocalDate suspensoEm;
    /** Qual das causas do art. 8.o justificou a interrupcao. */
    private String suspensaoMotivo;
    /** V58: pedido em horas (HH:mm), nulo num pedido de dias inteiros. */
    private String horaInicio;
    private String horaFim;
    /** Minutos por dia de um pedido em horas; zero num de dias inteiros. */
    private int minutosPorDia;
}

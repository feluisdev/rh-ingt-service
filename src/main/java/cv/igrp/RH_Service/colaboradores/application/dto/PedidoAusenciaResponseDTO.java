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
    private String estado;
    private String aprovadoPor;
    private LocalDate dataDecisao;
    private String observacoesDecisao;
    private Boolean isActive;
    private String estadoDesc;
}

package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class HistoricoEstadoColaboradorResponseDTO {
    private String id;
    private String funcionarioId;
    private String estadoAnteriorId;
    private String estadoAnteriorDescricao;
    private String estadoNovoId;
    private String estadoNovoDescricao;
    private String motivoCkey;
    private String motivoDescricao;
    private LocalDate dataEfectividade;
    private String observacao;
    private String registadoPor;
    private LocalDateTime registadoEm;
}

package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class MudarEstadoColaboradorRequestDTO {
    private String workerStateId;
    private String motivoCkey;
    private LocalDate dataEfectividade;
    private String observacao;
}

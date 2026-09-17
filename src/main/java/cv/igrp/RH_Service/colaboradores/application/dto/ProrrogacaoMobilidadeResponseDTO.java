package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProrrogacaoMobilidadeResponseDTO {
    private String id;
    private LocalDate dataInicio;
    private LocalDate dataFimAnterior;
    private LocalDate dataFim;
    /** Prorrogações já concedidas, incluindo esta. */
    private Integer prorrogacoes;
    /** Limite parametrizado no subtipo; nulo = sem limite. */
    private Integer maxProrrogacoes;
}

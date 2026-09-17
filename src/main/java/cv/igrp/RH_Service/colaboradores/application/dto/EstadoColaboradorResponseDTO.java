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
public class EstadoColaboradorResponseDTO {
    private String funcionarioId;
    private String estadoAnteriorId;
    private String estadoNovoId;
    private String estadoNovoCode;
    private LocalDate dataEfectividade;
    /** true = o estado termina a relação de emprego público; o contrato e a afectação foram encerrados. */
    private Boolean cessouVinculo;
    private String contratoId;
    /** Afectação encerrada pela cessação — o Lugar voltou a vago. Nulo se não havia afectação. */
    private String afectacaoEncerradaId;
}

package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Lançar ou pedir trabalho suplementar: um intervalo de um dia, com motivo. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class TrabalhoSuplementarRequestDTO {
    /** Só na chefia ({@code /me/equipa}): de quem é. No RH vem no caminho; no próprio é o utilizador. */
    private String funcionarioId;
    private LocalDate data;
    /** HH:mm. */
    private String horaInicio;
    /** HH:mm, depois do início (no mesmo dia). */
    private String horaFim;
    /** Obrigatório: porque é preciso. */
    private String motivo;
}

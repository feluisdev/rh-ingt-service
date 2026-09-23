package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioResponseDTO;
import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class HorarioVigenteResponseDTO {
    private String funcionarioId;
    private LocalDate data;
    /** COLABORADOR, UNIDADE, BASE ou NENHUM: de onde vem o horário. */
    private String origem;
    /** Nulo quando a origem é NENHUM. */
    private String regimePrestacao;
    /** A atribuição ao colaborador, quando a origem é COLABORADOR. */
    private String atribuicaoId;
    private HorarioResponseDTO horario;
}

package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class HorarioResponseDTO {
    private String id;
    private String nome;
    private String controlo;
    private String periodoAfericao;
    private String duracaoDiaria;
    private List<HorarioBlocoDTO> blocos;
    /** HH:mm, calculado: soma dos blocos (fixo) ou duração diária × dias com blocos (flexível). */
    private String horasSemanais;
    /** O horário da instituição, para quem não tem horário na pessoa nem na unidade. */
    private Boolean isBase;
    private Boolean isActive;
    private String estadoDesc;
}

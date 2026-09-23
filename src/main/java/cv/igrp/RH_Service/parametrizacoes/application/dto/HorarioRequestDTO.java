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
public class HorarioRequestDTO {
    private String nome;
    /** FIXO ou FLEXIVEL. */
    private String controlo;
    /** SEMANA ou MES; só no flexível. */
    private String periodoAfericao;
    /** HH:mm; só no flexível. */
    private String duracaoDiaria;
    private List<HorarioBlocoDTO> blocos;
}

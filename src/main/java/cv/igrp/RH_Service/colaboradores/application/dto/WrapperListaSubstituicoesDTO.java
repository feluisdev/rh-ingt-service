package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WrapperListaSubstituicoesDTO {
    private List<SubstituicaoLinhaDTO> linhas;
    private int total;
}

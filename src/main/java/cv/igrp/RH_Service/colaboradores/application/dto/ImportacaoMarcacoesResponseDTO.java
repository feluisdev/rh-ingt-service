package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ImportacaoMarcacoesResponseDTO {
    private int importadas;
    /** Já importadas antes (mesma referência): não se duplicam. */
    private int duplicadas;
    private List<RejeicaoImportacaoDTO> rejeitadas = new ArrayList<>();
}

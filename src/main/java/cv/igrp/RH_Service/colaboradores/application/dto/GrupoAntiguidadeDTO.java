package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/** Um cargo da lista de antiguidade: carreira e categoria (ou, fora da grelha, o cargo do Lugar). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class GrupoAntiguidadeDTO {
    private String carreira;
    private String categoria;
    private boolean foraDeGrelha;
    private List<LinhaAntiguidadeDTO> linhas;
}

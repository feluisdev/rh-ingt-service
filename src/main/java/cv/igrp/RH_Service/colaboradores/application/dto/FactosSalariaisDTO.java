package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Os factos que entram num mês de processamento — o contrato de leitura com o salarial (versão 1). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class FactosSalariaisDTO {
    /** Versão do contrato: muda quando a forma muda de modo incompatível. */
    private int versao;
    private String mes;
    private int total;
    private List<FactoRhDTO> factos = new ArrayList<>();
}

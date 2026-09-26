package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** O fecho de um mês de processamento. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class FechoMensalDTO {
    /** AAAA-MM */
    private String mes;
    /** FECHADO, REABERTO */
    private String estado;
    private LocalDateTime fechadoEm;
    /** Quantas vezes foi fechado. */
    private int fechos;
    private String motivoReabertura;
    private LocalDateTime reabertoEm;
    /** Os factos do mês no momento do fecho. */
    private int totalFactos;
    /** As linhas da relação mensal fotografadas. */
    private int linhas;
    private List<String> alertas = new ArrayList<>();
}

package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AssiduidadeResponseDTO {
    private String funcionarioId;
    private LocalDate de;
    private LocalDate ate;
    private List<DiaAssiduidadeDTO> dias = new ArrayList<>();
    private List<SemanaAssiduidadeDTO> semanas = new ArrayList<>();
}

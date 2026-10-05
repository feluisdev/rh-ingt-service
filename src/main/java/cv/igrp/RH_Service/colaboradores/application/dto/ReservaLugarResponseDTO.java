package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Lugar reservado para quem foi registado sem contrato (BR-AF-23): fica ocupado quando o contrato for registado. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ReservaLugarResponseDTO {
    private String id;
    private String funcionarioId;
    private String positionId;
    private String numeroLugar;
    private String unidadeOrganicaId;
    private String gradeId;
    private String functionId;
    private String notes;
    /** ACTIVA, CONCLUIDA, CANCELADA. */
    private String estado;
    private LocalDate reservadaEm;
    private LocalDate fechadaEm;
    /** Porque foi cancelada. */
    private String motivo;
    /** A afectação em que se concretizou. */
    private String assignmentId;
}

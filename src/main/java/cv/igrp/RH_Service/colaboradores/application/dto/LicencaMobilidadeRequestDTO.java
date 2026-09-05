package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class LicencaMobilidadeRequestDTO {

    @NotNull
    private UUID subtipoId;

    @NotNull
    private LocalDate dataInicio;

    private LocalDate dataFim;

    private String entidadeDestino;
    private String despachoNumero;
    private String observacoes;
    private String justification;
    private java.util.UUID destinationUnitId;
    /** Lugar (Position) de destino — obrigatório para mobilidade que muda de cadeira. */
    private java.util.UUID destinationPositionId;
}

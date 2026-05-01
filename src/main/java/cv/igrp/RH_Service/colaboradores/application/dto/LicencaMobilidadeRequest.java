package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class LicencaMobilidadeRequest {

    @NotNull
    private UUID subtipoId;

    @NotNull
    private LocalDate dataInicio;

    private LocalDate dataFim;

    private String entidadeDestino;
    private String despachoNumero;
    private String observacoes;
}

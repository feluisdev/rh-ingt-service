package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SelfServiceCriarLicencaMobilidadeRequest {
    @NotNull
    private UUID subtipoId;
    @NotNull
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String entidadeDestino;
    private String despachoNumero;
    private String observacoes;
    private String justification;
}

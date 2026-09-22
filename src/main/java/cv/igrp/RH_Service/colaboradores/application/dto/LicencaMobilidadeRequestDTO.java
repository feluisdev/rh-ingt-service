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
    /**
     * Como a mobilidade é prestada (art. 134.º n.º 2): {@code TEMPO_INTEIRO} ou
     * {@code ACUMULACAO}. Omisso vale {@code TEMPO_INTEIRO} — a exclusividade é a regra
     * (art. 20.º). Só faz sentido numa mobilidade; numa licença é recusado.
     */
    private String formaPrestacao;
}

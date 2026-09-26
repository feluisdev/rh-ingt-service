package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Um acidente em serviço ou doença profissional. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AcidenteServicoDTO {
    private String id;
    private String funcionarioId;
    private String funcionarioNome;
    /** ACIDENTE_SERVICO, ACIDENTE_TRAJECTO, DOENCA_PROFISSIONAL */
    private String tipo;
    private LocalDateTime dataHora;
    private String local;
    private String descricao;
    private String testemunhas;
    private LocalDate dataParticipacao;
    private boolean participadoPeloProprio;
    /** PARTICIPADO, QUALIFICADO, NAO_QUALIFICADO, ENCERRADO */
    private String estado;
    private String despacho;
    private String motivo;
    private String seguradora;
    private String apolice;
    private LocalDate participacaoSeguradora;
    private List<IncapacidadeAcidenteDTO> incapacidades = new ArrayList<>();
    private LocalDate alta;
    private BigDecimal incapacidadePermanente;
    private boolean incapacidadeAbsoluta;
    private List<String> alertas = new ArrayList<>();
}

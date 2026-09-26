package cv.igrp.RH_Service.formacao.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Uma acção de formação com as inscrições (em /me, só a própria). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AccaoFormacaoDTO {
    private String id;
    private String planoId;
    private List<String> necessidades = new ArrayList<>();
    private String tema;
    private String entidadeFormadora;
    /** PRESENCIAL, DISTANCIA, MISTA */
    private String modalidade;
    private boolean interna;
    private LocalDate inicio;
    private LocalDate fim;
    private Integer horas;
    private String horario;
    private String local;
    private Integer vagas;
    /** Informativo. */
    private BigDecimal custoPrevisto;
    private boolean custeadaPelaAdministracao;
    private Integer mesesGarantia;
    /** PLANEADA, INSCRICOES_ABERTAS, EM_CURSO, CONCLUIDA, CANCELADA */
    private String estado;
    private String motivoCancelamento;
    private int admitidos;
    private List<InscricaoFormacaoDTO> inscricoes = new ArrayList<>();
}

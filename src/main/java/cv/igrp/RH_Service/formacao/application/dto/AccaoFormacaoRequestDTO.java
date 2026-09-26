package cv.igrp.RH_Service.formacao.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Os termos de uma acção, uma inscrição, uma decisão ou uma avaliação (cada acção usa os seus). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AccaoFormacaoRequestDTO {
    private String tema;
    private String entidadeFormadora;
    /** PRESENCIAL, DISTANCIA, MISTA */
    private String modalidade;
    private Boolean interna;
    private LocalDate inicio;
    private LocalDate fim;
    private Integer horas;
    private String horario;
    private String local;
    private Integer vagas;
    private BigDecimal custoPrevisto;
    private Boolean custeadaPelaAdministracao;
    /** Só quando a Administração custeia (1 a 60). */
    private Integer mesesGarantia;
    private String planoId;
    /** As necessidades do plano a que a acção responde. */
    private List<String> necessidades = new ArrayList<>();
    /** Inscrever (em /me, a chefia inscreve alguém da equipa; vazio = o próprio). */
    private String funcionarioId;
    /** Recusar, desistir, cancelar. */
    private String motivo;
    /** Avaliar: APROVEITAMENTO, SEM_APROVEITAMENTO, FALTOU. */
    private String resultado;
    /** Avaliar. */
    private Integer diasPresenca;
}

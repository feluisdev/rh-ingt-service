package cv.igrp.RH_Service.recrutamento.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Uma candidatura a um concurso. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class CandidaturaDTO {
    private String id;
    private String concursoId;
    private String nome;
    private String documento;
    private String nif;
    private String email;
    private String telefone;
    private String habilitacao;
    private boolean deficiencia;
    /** O candidato é funcionário da casa. */
    private String funcionarioId;
    private boolean vinculadoAdministracao;
    private LocalDate dataApresentacao;
    /** APRESENTADA, EM_AUDIENCIA, ADMITIDA, EXCLUIDA, REPROVADA, APROVADA, PROVIDA, DESISTIU */
    private String estado;
    private String motivoExclusao;
    private LocalDate audienciaAte;
    private String respostaAudiencia;
    private List<NotaCandidaturaDTO> notas = new ArrayList<>();
    private BigDecimal classificacaoFinal;
    private Integer posicao;
    private String lugarProvidoId;
    private LocalDate dataDesistencia;
}

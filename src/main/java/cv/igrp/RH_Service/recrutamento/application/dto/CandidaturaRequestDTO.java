package cv.igrp.RH_Service.recrutamento.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Registar uma candidatura e os passos seguintes (exclusão, audiência, nota, provimento). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class CandidaturaRequestDTO {
    private String nome;
    private String documento;
    private String nif;
    private String email;
    private String telefone;
    private String habilitacao;
    private Boolean deficiencia;
    /** O candidato é da casa. */
    private String funcionarioId;
    private Boolean vinculadoAdministracao;
    /** Propor a exclusão. */
    private String motivo;
    /** Decidir a audiência: excluir de vez (true) ou admitir (false). */
    private Boolean excluir;
    /** A resposta do candidato na audiência. */
    private String resposta;
    /** Nota. */
    private String metodo;
    /** Nota (0 a 20). */
    private BigDecimal nota;
    /** Prover. */
    private String lugarId;
}

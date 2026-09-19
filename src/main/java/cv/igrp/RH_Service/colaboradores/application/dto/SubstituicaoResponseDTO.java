package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Resultado da substituição. Leva o titular substituído porque é o dado que o ecrã
 * precisa de mostrar e que não se deduz do Lugar — e porque é dele que depende o fim
 * da substituição.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubstituicaoResponseDTO {
    /** Id da afectação de substituição criada. */
    private String id;
    private String funcionarioId;
    private String positionId;
    private String numeroLugar;
    private String unidadeOrganicaId;
    private String gradeId;
    private String functionId;
    private LocalDate dataInicio;

    /** Quem está a ser substituído. */
    private String titularId;
    private String titularNome;
    /** Afectação do titular que esta substituição cobre; é o que a faz caducar. */
    private String titularAssignmentId;
}

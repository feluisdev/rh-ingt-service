package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QualificacaoResponseDTO {
    private String id;
    private String funcionarioId;
    private String nivelAcademico;
    private String curso;
    private String instituicao;
    private Integer anoConclusao;
    private String pais;
    private Boolean isActive;
}

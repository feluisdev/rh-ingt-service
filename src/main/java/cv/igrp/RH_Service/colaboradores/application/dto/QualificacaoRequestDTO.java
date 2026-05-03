package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QualificacaoRequestDTO {
    private String funcionarioId;
    @NotBlank
    private String nivelAcademico;
    @NotBlank
    private String curso;
    private String instituicao;
    private Integer anoConclusao;
    private String pais;
}

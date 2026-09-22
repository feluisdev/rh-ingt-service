package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class MudancaCarreiraRequestDTO {
    /** Lugar vago da carreira de destino. A carreira tem de ser diferente da actual. */
    @NotBlank(message = "O campo positionId é obrigatório")
    private String positionId;
    /**
     * Opcional: escalão de entrada na nova categoria. Tem de pertencer à categoria do Lugar de
     * destino; por omissão usa-se o primeiro escalão activo dessa categoria. Quem posiciona é o
     * acto administrativo — o critério legal assenta em remuneração, que a aplicação não tem.
     */
    private String gradeId;
    /** Opcional: por omissão mantém-se a função actual, se for compatível com o cargo do destino. */
    private String functionId;
    @NotNull(message = "O campo dataEfeito é obrigatório")
    private LocalDate dataEfeito;
    private String despachoNumero;
    private String concursoRef;
    private String observacoes;
}

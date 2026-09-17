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
public class PromocaoRequestDTO {
    @NotBlank(message = "O campo categoryId é obrigatório")
    private String categoryId;
    /** Presente = muda de Lugar; ausente = o Lugar actual sobe de categoria (reclassificação). */
    private String positionId;
    /** Opcional: por omissão, o primeiro escalão activo da categoria de destino. */
    private String gradeId;
    @NotNull(message = "O campo dataEfeito é obrigatório")
    private LocalDate dataEfeito;
    private String despachoNumero;
    private String concursoRef;
    private String observacoes;
}

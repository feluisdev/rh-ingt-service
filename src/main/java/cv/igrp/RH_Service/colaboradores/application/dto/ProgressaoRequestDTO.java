package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ProgressaoRequestDTO {
    @NotNull(message = "O campo dataEfeito é obrigatório")
    private LocalDate dataEfeito;
    private String despachoNumero;
    private String observacoes;
}

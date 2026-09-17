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
public class TransferenciaRequestDTO {
    @NotBlank(message = "O campo positionId é obrigatório")
    private String positionId;
    /** Opcional: por omissão mantém-se a função actual, se for compatível com o cargo do destino. */
    private String functionId;
    @NotNull(message = "O campo dataEfeito é obrigatório")
    private LocalDate dataEfeito;
    private String despachoNumero;
    private String observacoes;
}

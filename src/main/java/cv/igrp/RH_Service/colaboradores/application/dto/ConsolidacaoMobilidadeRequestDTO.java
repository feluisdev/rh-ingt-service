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
public class ConsolidacaoMobilidadeRequestDTO {
    /**
     * Lugar vago do serviço de destino, do mesmo cargo e da mesma categoria do actual
     * (art. 132.º n.º 4: «na mesma função e categoria»).
     */
    @NotBlank(message = "O campo positionId é obrigatório")
    private String positionId;
    /** Primeiro dia em que a pessoa é titular do Lugar de destino; a mobilidade acaba na véspera. */
    @NotNull(message = "O campo dataEfeito é obrigatório")
    private LocalDate dataEfeito;
    private String despachoNumero;
    private String observacoes;
}

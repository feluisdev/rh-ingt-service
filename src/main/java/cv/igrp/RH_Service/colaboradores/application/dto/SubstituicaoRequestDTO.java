package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Pedido de substituição: pôr este colaborador a substituir o titular de um Lugar.
 *
 * <p>Não tem data de fim. A substituição termina quando cessa o impedimento do titular
 * (art. 77.º n.º 2), e não numa data combinada à parte — quem a fecha é o regresso dele.
 */
@Getter
@Setter
@NoArgsConstructor
public class SubstituicaoRequestDTO {

    /** Lugar a substituir. Tem de ter titular, e o titular tem de estar impedido. */
    @NotBlank(message = "O campo positionId é obrigatório")
    private String positionId;

    /** Obrigatório em Lugar de carreira, proibido em Lugar fora de grelha. */
    private String gradeId;

    /** Opcional: a função a exercer, que tem de pertencer ao cargo do Lugar. */
    private String functionId;

    @NotNull(message = "O campo dataInicio é obrigatório")
    private LocalDate dataInicio;

    private String despachoNumero;
    private String observacoes;
}

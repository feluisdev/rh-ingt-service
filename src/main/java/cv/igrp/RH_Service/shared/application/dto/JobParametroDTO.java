package cv.igrp.RH_Service.shared.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
@Schema(description = "Parâmetro que o job aceita no disparo manual. A interface gera o formulário a partir desta lista, "
        + "pelo que um job novo com parâmetros novos não obriga a alterar o frontend")
public class JobParametroDTO {

    @Schema(description = "Nome técnico do parâmetro, usado como chave no corpo do pedido de execução", example = "data")
    private String nome;

    @Schema(description = "Rótulo a apresentar no formulário", example = "Data de referência")
    private String rotulo;

    @Schema(description = "Tipo do parâmetro; determina o controlo a desenhar", example = "DATA",
            allowableValues = {"DATA", "ANO", "TEXTO", "INTEIRO", "BOOLEANO", "UUID"})
    private String tipo;

    @Schema(description = "Se falso, o job assume um valor por omissão quando o parâmetro não é indicado", example = "false")
    private Boolean obrigatorio;

    @Schema(description = "Texto de ajuda a mostrar junto ao campo", example = "Se vazio, usa o dia do agendamento.")
    private String ajuda;

    @Schema(description = "Exemplo de valor válido", example = "2026-01-01")
    private String exemplo;
}

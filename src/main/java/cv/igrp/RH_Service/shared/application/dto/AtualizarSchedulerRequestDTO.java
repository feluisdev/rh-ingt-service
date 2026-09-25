package cv.igrp.RH_Service.shared.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
@Schema(description = "Novo agendamento de um job. Os campos obrigatórios dependem da frequência: "
        + "DIARIO → hora, minuto; SEMANAL → diaDaSemana, hora, minuto; QUINZENAL → hora, minuto (dias 1 e 15); "
        + "MENSAL, TRIMESTRAL (Jan, Abr, Jul, Out), SEMESTRAL (Jan, Jul) → diaDoMes, hora, minuto; "
        + "ANUAL → diaDoMes, mes, hora, minuto")
public class AtualizarSchedulerRequestDTO {

    @NotBlank(message = "Escolha a frequência.")
    @Schema(example = "DIARIO",
            allowableValues = {"DIARIO", "SEMANAL", "QUINZENAL", "MENSAL", "TRIMESTRAL", "SEMESTRAL", "ANUAL"})
    private String frequencia;

    @Schema(description = "Dia do mês (1-28). Obrigatório para MENSAL, TRIMESTRAL, SEMESTRAL e ANUAL", example = "5")
    private Integer diaDoMes;

    @Schema(description = "Dia da semana. Obrigatório para SEMANAL", example = "MON",
            allowableValues = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"})
    private String diaDaSemana;

    @Schema(description = "Mês do ano (1-12). Obrigatório para ANUAL", example = "6")
    private Integer mes;

    @NotNull(message = "Indique a hora.")
    @Schema(description = "Hora de execução (0-23)", example = "0")
    private Integer hora;

    @NotNull(message = "Indique o minuto.")
    @Schema(description = "Minuto de execução (0-59)", example = "5")
    private Integer minuto;

    @Schema(description = "Fuso horário em que o cron passa a ser interpretado. Se omitido, mantém-se o actual",
            example = "Atlantic/Cape_Verde")
    private String timezone;
}

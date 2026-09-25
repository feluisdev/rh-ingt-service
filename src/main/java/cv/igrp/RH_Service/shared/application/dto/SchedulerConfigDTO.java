package cv.igrp.RH_Service.shared.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
@Schema(description = "Configuração actual de um job agendado: frequência, hora, estado, última e próxima execução, "
        + "e os parâmetros que aceita no disparo manual")
public class SchedulerConfigDTO {

    @Schema(description = "Identificador estável do job", example = "RH_VENCIMENTO_FERIAS")
    private String chave;

    @Schema(description = "Nome legível do job", example = "Vencimento do direito a férias")
    private String nome;

    @Schema(description = "Código da frequência", example = "DIARIO",
            allowableValues = {"DIARIO", "SEMANAL", "QUINZENAL", "MENSAL", "TRIMESTRAL", "SEMESTRAL", "ANUAL"})
    private String frequencia;

    @Schema(description = "Descrição legível da frequência", example = "Diário")
    private String frequenciaDesc;

    @Schema(description = "Dia do mês (1-28), quando a frequência usa dia do mês", example = "5")
    private Integer diaDoMes;

    @Schema(description = "Descrição legível do dia do mês", example = "Dia 5 do mês")
    private String diaDoMesDesc;

    @Schema(description = "Código do dia da semana, quando a frequência é SEMANAL", example = "MON",
            allowableValues = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"})
    private String diaDaSemana;

    @Schema(description = "Descrição legível do dia da semana", example = "Segunda-feira")
    private String diaDaSemanaDesc;

    @Schema(description = "Mês do ano (1-12), quando a frequência é ANUAL", example = "6")
    private Integer mes;

    @Schema(description = "Descrição legível do mês", example = "junho")
    private String mesDesc;

    @Schema(description = "Hora de execução (0-23)", example = "0")
    private Integer hora;

    @Schema(description = "Minuto de execução (0-59)", example = "5")
    private Integer minuto;

    @Schema(description = "Hora formatada HH:mm", example = "00:05")
    private String horaDesc;

    @Schema(description = "Resumo do agendamento em linguagem natural", example = "Diário às 00:05")
    private String descricao;

    @Schema(description = "Fuso horário em que o cron é interpretado", example = "Atlantic/Cape_Verde")
    private String timezone;

    @Schema(description = "Job activo. Um job inactivo não é agendado nem gera execuções em falta", example = "true")
    private Boolean activo;

    @Schema(description = "Tentativas automáticas em caso de FALHA ou TIMEOUT. 0 = sem retry automático", example = "3")
    private Integer maxTentativas;

    @Schema(description = "Tempo limite de uma execução, em segundos", example = "1800")
    private Integer timeoutSegundos;

    @Schema(description = "Instante em que o job correu pela última vez", example = "2026-09-25T00:05:03")
    private LocalDateTime ultimaExecucao;

    @Schema(description = "Desfecho da última execução", example = "SUCESSO",
            allowableValues = {"A_CORRER", "SUCESSO", "FALHA_PARCIAL", "FALHA", "TIMEOUT", "OMITIDA"})
    private String ultimoEstado;

    @Schema(description = "Instante do próximo disparo previsto. Nulo quando o job está inactivo", example = "2026-09-26T00:05:00")
    private LocalDateTime proximaExecucao;

    @Schema(description = "Parâmetros que o job aceita no disparo manual")
    private List<JobParametroDTO> parametros;
}

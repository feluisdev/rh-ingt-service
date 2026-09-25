package cv.igrp.RH_Service.shared.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
@Schema(description = "Uma execução de um job: quando correu, quanto tempo levou, o que produziu e porque falhou")
public class SchedulerExecucaoResponseDTO {

    private UUID id;

    @Schema(example = "RH_VENCIMENTO_FERIAS")
    private String chave;

    private String nome;

    private String cron;

    @Schema(allowableValues = {"AGENDADO", "MANUAL"})
    private String disparo;

    @Schema(description = "Quem disparou à mão; vazio nas execuções agendadas")
    private String solicitante;

    @Schema(description = "Réplica que correu a execução")
    private String instancia;

    @Schema(description = "Instante do cron a que esta execução corresponde. Para execuções agendadas é a hora prevista, "
            + "não a hora a que arrancou — é dele que deriva o período processado", example = "2026-09-25T00:05:00")
    private LocalDateTime agendadoPara;

    private LocalDateTime inicio;

    private LocalDateTime fim;

    private Long duracaoMs;

    @Schema(allowableValues = {"A_CORRER", "SUCESSO", "FALHA_PARCIAL", "FALHA", "TIMEOUT", "OMITIDA"})
    private String estado;

    @Schema(description = "Stack trace resumido, quando a execução falhou")
    private String erro;

    private Integer processados;

    private Integer criados;

    private Integer repetidos;

    private Integer saltados;

    private Integer falhas;

    @Schema(description = "Contexto de negócio reportado pelo job", example = "2026")
    private String referencia;

    private String mensagem;

    @Schema(description = "Parâmetros com que a execução correu. Gravados no arranque, pelo que existem mesmo "
            + "quando o job rebenta; são a base da re-execução", example = "{\"data\": \"2026-09-25\"}")
    private Map<String, Object> parametros;

    @Schema(description = "Métricas adicionais reportadas pelo job, incluindo a lista de itens falhados (limitada a 200)")
    private Map<String, Object> detalhes;

    @Schema(description = "Número da tentativa dentro da mesma origem (1 = primeira)", example = "1")
    private Integer tentativa;

    @Schema(description = "Execução que deu origem a esta — retry automático ou re-execução manual")
    private UUID execucaoPaiId;
}

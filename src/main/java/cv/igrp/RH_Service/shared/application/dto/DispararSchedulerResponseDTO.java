package cv.igrp.RH_Service.shared.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
@Schema(description = "Execução aceite. O desfecho consulta-se depois por GET /schedulers/execucoes/{id}")
public class DispararSchedulerResponseDTO {

    @Schema(description = "Identificador do registo de execução")
    private UUID id;

    private String chave;

    @Schema(example = "A_CORRER")
    private String estado;

    @Schema(description = "Parâmetros com que a execução foi aberta", example = "{\"data\": \"2026-09-25\"}")
    private Map<String, Object> parametros;

    @Schema(description = "Execução repetida, quando o disparo veio de uma re-execução")
    private UUID execucaoPaiId;
}

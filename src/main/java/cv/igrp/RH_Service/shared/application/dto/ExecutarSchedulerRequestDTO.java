package cv.igrp.RH_Service.shared.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
@Schema(description = "Parâmetros do disparo manual. O corpo é opcional: sem ele, cada parâmetro assume o valor por omissão do job")
public class ExecutarSchedulerRequestDTO {

    @Schema(description = "Parâmetros da execução, pelo nome declarado em GET /schedulers/{chave}",
            example = "{\"data\": \"2026-09-25\"}")
    private Map<String, Object> parametros;
}

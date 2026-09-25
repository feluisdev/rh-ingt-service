package cv.igrp.RH_Service.shared.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
@Schema(description = "Activa ou suspende um job sem perder a sua configuração de agendamento")
public class AlterarEstadoSchedulerRequestDTO {

    @NotNull(message = "Indique se a tarefa fica activa ou suspensa.")
    @Schema(description = "true activa o job (recalculando a próxima execução a partir de agora); false suspende-o",
            example = "false")
    private Boolean activo;
}

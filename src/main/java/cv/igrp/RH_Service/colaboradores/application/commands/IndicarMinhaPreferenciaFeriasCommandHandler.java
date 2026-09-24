package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.MapaFeriasService;
import cv.igrp.RH_Service.colaboradores.domain.models.OrigemPreferenciaFerias;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class IndicarMinhaPreferenciaFeriasCommandHandler
        implements CommandHandler<IndicarMinhaPreferenciaFeriasCommand, ResponseEntity<SuccessResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final MapaFeriasService mapaFeriasService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(IndicarMinhaPreferenciaFeriasCommand command) {
        var dto = command.getRequest();
        var resultado = mapaFeriasService.indicarPreferencia(currentEmployeeResolver.resolve(), command.getAno(),
                IndicarPreferenciaFeriasCommandHandler.periodos(dto != null ? dto.getPeriodos() : null),
                dto != null ? dto.getObservacoes() : null, OrigemPreferenciaFerias.PROPRIO);
        return ResponseEntity.ok(new SuccessResponseDTO(
                resultado.ferias().getId().getStringValor(), true, new ArrayList<>(resultado.alertas())));
    }
}

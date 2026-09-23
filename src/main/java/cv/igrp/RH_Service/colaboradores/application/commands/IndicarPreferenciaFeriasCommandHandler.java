package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PeriodoFeriasDTO;
import cv.igrp.RH_Service.colaboradores.application.services.MapaFeriasService;
import cv.igrp.RH_Service.colaboradores.domain.models.PeriodoFerias;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Preferência de férias — DL n.º 3/2010, art. 5.º n.º 4. */
@Component
@RequiredArgsConstructor
public class IndicarPreferenciaFeriasCommandHandler
        implements CommandHandler<IndicarPreferenciaFeriasCommand, ResponseEntity<SuccessResponseDTO>> {

    private final MapaFeriasService mapaFeriasService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(IndicarPreferenciaFeriasCommand command) {
        var dto = command.getRequest();
        var resultado = mapaFeriasService.indicarPreferencia(
                FuncionarioId.from(command.getFuncionarioId()), command.getAno(),
                periodos(dto.getPeriodos()), dto.getObservacoes());
        return ResponseEntity.ok(new SuccessResponseDTO(
                resultado.ferias().getId().getStringValor(), true, new ArrayList<>(resultado.alertas())));
    }

    static List<PeriodoFerias> periodos(List<PeriodoFeriasDTO> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream().map(p -> new PeriodoFerias(p.getDataInicio(), p.getDataFim(), null)).toList();
    }
}

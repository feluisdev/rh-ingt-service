package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.MapaFeriasService;
import cv.igrp.RH_Service.colaboradores.domain.models.MotivoAlteracaoMapaFerias;
import cv.igrp.RH_Service.colaboradores.domain.models.OrigemMarcacaoFerias;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/** Marcação das férias — DL n.º 3/2010, arts. 5.º e 6.º n.º 2. */
@Component
@RequiredArgsConstructor
public class MarcarFeriasCommandHandler
        implements CommandHandler<MarcarFeriasCommand, ResponseEntity<SuccessResponseDTO>> {

    private final MapaFeriasService mapaFeriasService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(MarcarFeriasCommand command) {
        var dto = command.getRequest();
        var resultado = mapaFeriasService.marcar(
                FuncionarioId.from(command.getFuncionarioId()), command.getAno(),
                IndicarPreferenciaFeriasCommandHandler.periodos(dto.getPeriodos()),
                OrigemMarcacaoFerias.de(dto.getOrigem()),
                dto.getFundamentacao(),
                MotivoAlteracaoMapaFerias.de(dto.getMotivoAlteracao()));
        return ResponseEntity.ok(new SuccessResponseDTO(
                resultado.ferias().getId().getStringValor(), true, new ArrayList<>(resultado.alertas())));
    }
}

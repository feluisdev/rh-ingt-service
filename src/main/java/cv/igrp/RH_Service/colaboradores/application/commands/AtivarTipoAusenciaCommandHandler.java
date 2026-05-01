package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsAtivarTipoAusenciaCommandHandler")
@RequiredArgsConstructor
public class AtivarTipoAusenciaCommandHandler
        implements CommandHandler<AtivarTipoAusenciaCommand, ResponseEntity<Map<String, ?>>> {

    private final TipoAusenciaRepository tipoAusenciaRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AtivarTipoAusenciaCommand command) {
        var tipo = tipoAusenciaRepository.findById(TipoAusenciaId.from(command.getId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo de ausência não encontrado: " + command.getId()));
        tipo.ativar();
        tipoAusenciaRepository.save(tipo);
        return ResponseEntity.ok(Map.of("message", "Activado com sucesso"));
    }
}

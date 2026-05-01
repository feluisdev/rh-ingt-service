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

@Component("colabsDesativarTipoAusenciaCommandHandler")
@RequiredArgsConstructor
public class DesativarTipoAusenciaCommandHandler
        implements CommandHandler<DesativarTipoAusenciaCommand, ResponseEntity<Map<String, ?>>> {

    private final TipoAusenciaRepository tipoAusenciaRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarTipoAusenciaCommand command) {
        var tipo = tipoAusenciaRepository.findById(TipoAusenciaId.from(command.getId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo de ausência não encontrado: " + command.getId()));
        tipo.desativar();
        tipoAusenciaRepository.save(tipo);
        return ResponseEntity.ok(Map.of("message", "Desactivado com sucesso"));
    }
}

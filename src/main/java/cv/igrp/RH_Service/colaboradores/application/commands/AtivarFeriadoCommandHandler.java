package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.FeriadoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FeriadoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsAtivarFeriadoCommandHandler")
@RequiredArgsConstructor
public class AtivarFeriadoCommandHandler
        implements CommandHandler<AtivarFeriadoCommand, ResponseEntity<Map<String, ?>>> {

    private final FeriadoRepository feriadoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AtivarFeriadoCommand command) {
        var feriado = feriadoRepository.findById(FeriadoId.from(command.getId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Feriado não encontrado: " + command.getId()));

        if (Boolean.TRUE.equals(feriado.getIsActive()))
            return ResponseEntity.ok(Map.of("message", "Activado com sucesso"));

        if (Boolean.TRUE.equals(feriado.getIsNational()) && feriadoRepository.existsNacionalActivoByData(feriado.getData()))
            throw IgrpResponseStatusException.conflict("Já existe um feriado nacional activo na data: " + feriado.getData());

        feriado.ativar();
        feriadoRepository.save(feriado);
        return ResponseEntity.ok(Map.of("message", "Activado com sucesso"));
    }
}

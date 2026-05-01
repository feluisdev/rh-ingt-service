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

@Component("colabsDesativarFeriadoCommandHandler")
@RequiredArgsConstructor
public class DesativarFeriadoCommandHandler
        implements CommandHandler<DesativarFeriadoCommand, ResponseEntity<Map<String, ?>>> {

    private final FeriadoRepository feriadoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarFeriadoCommand command) {
        var feriado = feriadoRepository.findById(FeriadoId.from(command.getId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Feriado não encontrado: " + command.getId()));
        feriado.desativar();
        feriadoRepository.save(feriado);
        return ResponseEntity.ok(Map.of("message", "Desactivado com sucesso"));
    }
}

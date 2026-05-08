package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.VinculoLaboral;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.VinculoLaboralRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.VinculoLaboralId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DesativarVinculoLaboralCommandHandler implements CommandHandler<DesativarVinculoLaboralCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesativarVinculoLaboralCommandHandler.class);

    private final VinculoLaboralRepository vinculoLaboralRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarVinculoLaboralCommand command) {
        var id = VinculoLaboralId.from(UUID.fromString(command.getVinculoLaboralId()));

        VinculoLaboral vinculoLaboral = vinculoLaboralRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + command.getVinculoLaboralId()));

        vinculoLaboral.desativar();
        vinculoLaboralRepository.save(vinculoLaboral);

        return ResponseEntity.ok(Map.of("message", "Desativado com sucesso"));
    }
}

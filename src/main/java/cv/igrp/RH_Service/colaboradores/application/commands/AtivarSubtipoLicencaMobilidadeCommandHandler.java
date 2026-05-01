package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsAtivarSubtipoLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class AtivarSubtipoLicencaMobilidadeCommandHandler
        implements CommandHandler<AtivarSubtipoLicencaMobilidadeCommand, ResponseEntity<Map<String, ?>>> {

    private final SubtipoLicencaMobilidadeRepository subtipoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AtivarSubtipoLicencaMobilidadeCommand command) {
        var subtipo = subtipoRepository.findById(SubtipoLicencaMobilidadeId.from(command.getId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Subtipo não encontrado: " + command.getId()));
        subtipo.ativar();
        subtipoRepository.save(subtipo);
        return ResponseEntity.ok(Map.of("message", "Activado com sucesso"));
    }
}

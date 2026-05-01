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

@Component("colabsDesativarSubtipoLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class DesativarSubtipoLicencaMobilidadeCommandHandler
        implements CommandHandler<DesativarSubtipoLicencaMobilidadeCommand, ResponseEntity<Map<String, ?>>> {

    private final SubtipoLicencaMobilidadeRepository subtipoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarSubtipoLicencaMobilidadeCommand command) {
        var subtipo = subtipoRepository.findById(SubtipoLicencaMobilidadeId.from(command.getId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Subtipo não encontrado: " + command.getId()));
        subtipo.desativar();
        subtipoRepository.save(subtipo);
        return ResponseEntity.ok(Map.of("message", "Desactivado com sucesso"));
    }
}

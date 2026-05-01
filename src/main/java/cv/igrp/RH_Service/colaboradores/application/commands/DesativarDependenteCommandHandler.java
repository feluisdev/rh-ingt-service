package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DependenteId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsDesativarDependenteCommandHandler")
@RequiredArgsConstructor
public class DesativarDependenteCommandHandler
        implements CommandHandler<DesativarDependenteCommand, ResponseEntity<Map<String, ?>>> {

    private final DependenteRepository dependenteRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarDependenteCommand command) {
        var id = DependenteId.from(command.getDependenteId());
        var dependente = dependenteRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Dependente não encontrado: " + command.getDependenteId()));

        if (Boolean.FALSE.equals(dependente.getIsActive()))
            throw IgrpResponseStatusException.badRequest("O dependente já está inactivo.");

        dependente.desativar();
        dependenteRepository.save(dependente);
        return ResponseEntity.ok(Map.of("message", "Desactivado com sucesso"));
    }
}

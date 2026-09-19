package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DependenteId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component("colabsAtivarDependenteCommandHandler")
@RequiredArgsConstructor
public class AtivarDependenteCommandHandler
        implements CommandHandler<AtivarDependenteCommand, ResponseEntity<SuccessResponseDTO>> {

    private final DependenteRepository dependenteRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(AtivarDependenteCommand command) {
        var id = DependenteId.from(command.getDependenteId());
        var dependente = dependenteRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Dependente não encontrado: " + command.getDependenteId()));

        if (Boolean.TRUE.equals(dependente.getIsActive()))
            return ResponseEntity.ok(SuccessResponseDTO.semEfeito(command.getDependenteId(), "Dependente já está activo."));

        dependente.ativar();
        dependenteRepository.save(dependente);
        return ResponseEntity.ok(SuccessResponseDTO.de(command.getDependenteId()));
    }
}

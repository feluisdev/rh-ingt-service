package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DesativarOrganizationalUnitCommandHandler
        implements CommandHandler<DesativarOrganizationalUnitCommand, ResponseEntity<SuccessResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesativarOrganizationalUnitCommandHandler.class);

    private final OrganizationalUnitRepository unitRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(DesativarOrganizationalUnitCommand command) {
        var id = OrganizationalUnitId.from(command.getUnitId());

        var unit = unitRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Unidade orgânica não encontrada: " + command.getUnitId()));

        if (unitRepository.existsActiveChildrenOf(id)) {
            throw IgrpResponseStatusException.conflict(
                    "Não é possível desactivar: a unidade orgânica tem sub-unidades activas.");
        }

        unit.desativar();
        unitRepository.save(unit);

        return ResponseEntity.ok(SuccessResponseDTO.de(command.getUnitId()));
    }
}

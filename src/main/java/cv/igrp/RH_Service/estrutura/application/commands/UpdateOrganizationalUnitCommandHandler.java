package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.application.dto.OrganizationalUnitResponseDTO;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.OrganizationalUnitMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateOrganizationalUnitCommandHandler
        implements CommandHandler<UpdateOrganizationalUnitCommand, ResponseEntity<OrganizationalUnitResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateOrganizationalUnitCommandHandler.class);

    private final OrganizationalUnitRepository unitRepository;
    private final OrganizationalUnitMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<OrganizationalUnitResponseDTO> handle(UpdateOrganizationalUnitCommand command) {
        var id = OrganizationalUnitId.from(command.getUnitId());
        var unit = unitRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Unidade orgânica não encontrada: " + command.getUnitId()));

        var dto = command.getRequest();

        if (unitRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe outra unidade orgânica com code='" + dto.getCode() + "'.");
        }

        OrganizationalUnitId parentId = null;
        if (dto.getParentUnitId() != null) {
            parentId = validateAndGetParentId(dto.getParentUnitId().toString(), id);
        }

        unit.atualizar(dto.getCode(), dto.getName(), dto.getAcronym(), dto.getUnitType(),
                dto.getDescricao(), dto.getEstado(), parentId);
        var updated = unitRepository.save(unit);

        return ResponseEntity.ok(mapper.toDTO(updated));
    }

    private OrganizationalUnitId validateAndGetParentId(String parentUnitIdStr, OrganizationalUnitId selfId) {
        var parentId = OrganizationalUnitId.from(parentUnitIdStr);
        if (parentId.equals(selfId)) {
            throw IgrpResponseStatusException.badRequest("Uma unidade não pode ser mãe de si própria.");
        }
        var parent = unitRepository.findById(parentId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Unidade-mãe não encontrada: " + parentUnitIdStr));
        if (!parent.isActive()) {
            throw IgrpResponseStatusException.conflict(
                    "Não é possível definir uma unidade inactiva como unidade-mãe.");
        }
        return parentId;
    }
}

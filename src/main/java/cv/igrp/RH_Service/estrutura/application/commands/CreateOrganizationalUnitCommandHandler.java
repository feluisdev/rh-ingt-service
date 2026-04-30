package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CreateOrganizationalUnitCommandHandler
        implements CommandHandler<CreateOrganizationalUnitCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateOrganizationalUnitCommandHandler.class);

    private final OrganizationalUnitRepository unitRepository;
    private final OptionRepository optionRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateOrganizationalUnitCommand command) {
        var dto = command.getRequest();

        if (unitRepository.existsByCode(dto.getCode())) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe uma unidade orgânica com code='" + dto.getCode() + "'.");
        }

        validateUnitTypeOption(dto.getUnitTypeOptionId());

        OrganizationalUnitId parentId = null;
        if (dto.getParentUnitId() != null) {
            parentId = validateAndGetParentId(dto.getParentUnitId().toString());
        }

        OrganizationalUnit saved = unitRepository.save(
                OrganizationalUnit.criar(dto.getCode(), dto.getName(), dto.getAcronym(),
                        dto.getUnitTypeOptionId(), parentId));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }

    private void validateUnitTypeOption(java.util.UUID unitTypeOptionId) {
        if (unitTypeOptionId == null) {
            throw IgrpResponseStatusException.badRequest("unitTypeOptionId é obrigatório.");
        }
        var options = optionRepository.findByCcodeAndLocale("UNIT_TYPE", "pt-CV", true);
        boolean valid = options.stream().anyMatch(o -> o.getId().getValor().equals(unitTypeOptionId));
        if (!valid) {
            throw IgrpResponseStatusException.badRequest(
                    "unitTypeOptionId inválido: não pertence ao grupo UNIT_TYPE.");
        }
    }

    private OrganizationalUnitId validateAndGetParentId(String parentUnitIdStr) {
        var parentId = OrganizationalUnitId.from(parentUnitIdStr);
        var parent = unitRepository.findById(parentId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Unidade-mãe não encontrada: " + parentUnitIdStr));
        if (!parent.isActive()) {
            throw IgrpResponseStatusException.conflict(
                    "Não é possível criar uma sub-unidade de uma unidade inactiva.");
        }
        return parentId;
    }
}

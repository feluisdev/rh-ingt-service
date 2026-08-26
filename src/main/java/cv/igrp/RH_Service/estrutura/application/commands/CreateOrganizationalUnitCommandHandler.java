package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
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
public class CreateOrganizationalUnitCommandHandler
        implements CommandHandler<CreateOrganizationalUnitCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateOrganizationalUnitCommandHandler.class);

    private final OrganizationalUnitRepository unitRepository;
    private final FuncionarioLookupPort funcionarioLookupPort;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateOrganizationalUnitCommand command) {
        var dto = command.getRequest();

        if (unitRepository.existsByCode(dto.getCode())) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe uma unidade orgânica com code='" + dto.getCode() + "'.");
        }

        OrganizationalUnitId parentId = null;
        if (dto.getParentUnitId() != null) {
            parentId = validateAndGetParentId(dto.getParentUnitId().toString());
        }

        UUID responsibleEmployeeId = validateAndGetResponsibleEmployeeId(dto.getResponsibleEmployeeId());

        OrganizationalUnit saved = unitRepository.save(
                OrganizationalUnit.criar(dto.getCode(), dto.getName(), dto.getAcronym(),
                        dto.getUnitType(), dto.getDescricao(), parentId, responsibleEmployeeId));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
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

    // Nao valida que o responsavel pertence a propria unidade -- nao-decisao
    // explicita do operador (D-06, 2026-08-24, ver 109-02-PLAN.md): os tres
    // funcionarios da base estao todos na mesma unidade, pelo que a restricao
    // nao seria exercida por dado nenhum. O que se valida e que o funcionario existe.
    private UUID validateAndGetResponsibleEmployeeId(UUID responsibleEmployeeId) {
        if (responsibleEmployeeId == null) {
            return null;
        }
        funcionarioLookupPort.findById(responsibleEmployeeId)
                .orElseThrow(() -> IgrpResponseStatusException.badRequest(
                        "Funcionário responsável não encontrado: " + responsibleEmployeeId));
        return responsibleEmployeeId;
    }
}

package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.ContractType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
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
public class CreateContractTypeCommandHandler implements CommandHandler<CreateContractTypeCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateContractTypeCommandHandler.class);

    private final ContractTypeRepository contractTypeRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateContractTypeCommand command) {
        var dto = command.getContractTypeRequest();

        if (contractTypeRepository.existsByCode(dto.getCode())) {
            throw IgrpResponseStatusException.conflict(
                "Já existe um registo com code='" + dto.getCode() + "'.");
        }

        UUID vinculoLaboralId = dto.getVinculoLaboralId() != null
                ? UUID.fromString(dto.getVinculoLaboralId()) : null;

        ContractType saved = contractTypeRepository.save(
            ContractType.criar(dto.getCode(), dto.getDescription(),
                vinculoLaboralId,
                Boolean.TRUE.equals(dto.getIsRenewable()),
                dto.getMaxRenewals(),
                dto.getMaxDurationMonths(),
                Boolean.TRUE.equals(dto.getRequiresCareerStructure()))
        );

        return ResponseEntity.status(201).body(Map.of(
            "id", saved.getId().getStringValor(),
            "message", "Criado com sucesso"
        ));
    }
}

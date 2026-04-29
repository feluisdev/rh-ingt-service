package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ContractTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContractType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ContractTypeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateContractTypeCommandHandler implements CommandHandler<UpdateContractTypeCommand, ResponseEntity<ContractTypeResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateContractTypeCommandHandler.class);

    private final ContractTypeRepository contractTypeRepository;
    private final ContractTypeMapper contractTypeMapper;

    @IgrpCommandHandler
    public ResponseEntity<ContractTypeResponseDTO> handle(UpdateContractTypeCommand command) {
        var id = ExternalID.from(UUID.fromString(command.getContractTypeId()));

        ContractType contractType = contractTypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + command.getContractTypeId()));

        contractType.atualizar(command.getContractTypeRequest().getDescription());
        ContractType saved = contractTypeRepository.save(contractType);

        return ResponseEntity.ok(contractTypeMapper.toDTO(saved));
    }
}

package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ContractTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContractType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ContractTypeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateContractTypeCommandHandler implements CommandHandler<UpdateContractTypeCommand, ResponseEntity<ContractTypeResponseDTO>> {

    private final ContractTypeRepository contractTypeRepository;
    private final ContractTypeMapper contractTypeMapper;

    @IgrpCommandHandler
    public ResponseEntity<ContractTypeResponseDTO> handle(UpdateContractTypeCommand command) {
        var id = ContractTypeId.from(UUID.fromString(command.getContractTypeId()));
        var req = command.getContractTypeRequest();

        ContractType contractType = contractTypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + command.getContractTypeId()));

        UUID vinculoLaboralId = req.getVinculoLaboralId() != null
                ? UUID.fromString(req.getVinculoLaboralId()) : null;

        contractType.atualizar(
            req.getDescription(),
            vinculoLaboralId,
            Boolean.TRUE.equals(req.getIsRenewable()),
            req.getMaxRenewals(),
            req.getMaxDurationMonths()
        );
        ContractType saved = contractTypeRepository.save(contractType);

        return ResponseEntity.ok(contractTypeMapper.toDTO(saved));
    }
}

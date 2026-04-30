package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.ContractType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
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
public class AtivarContractTypeCommandHandler implements CommandHandler<AtivarContractTypeCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtivarContractTypeCommandHandler.class);

    private final ContractTypeRepository contractTypeRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AtivarContractTypeCommand command) {
        var id = ContractTypeId.from(UUID.fromString(command.getContractTypeId()));

        ContractType contractType = contractTypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + command.getContractTypeId()));

        contractType.reativar();
        contractTypeRepository.save(contractType);

        return ResponseEntity.ok(Map.of("message", "Ativado com sucesso"));
    }
}

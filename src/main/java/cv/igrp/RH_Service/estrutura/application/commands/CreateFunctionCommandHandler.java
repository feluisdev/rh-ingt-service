package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.domain.models.OrgFunction;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateFunctionCommandHandler
        implements CommandHandler<CreateFunctionCommand, ResponseEntity<SuccessResponseDTO>> {

    private final FunctionRepository functionRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CreateFunctionCommand command) {
        var dto = command.getRequest();

        if (functionRepository.existsByCode(dto.getCode())) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe uma função com code='" + dto.getCode() + "'.");
        }

        UUID jobId = dto.getJobId() != null && !dto.getJobId().isBlank()
                ? UUID.fromString(dto.getJobId()) : null;

        OrgFunction saved = functionRepository.save(
                OrgFunction.criar(dto.getCode(), dto.getName(), dto.getDescription(), jobId));

        return ResponseEntity.status(201).body(SuccessResponseDTO.de(saved.getId().getStringValor()));
    }
}

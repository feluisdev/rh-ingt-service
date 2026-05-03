package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.application.dto.FunctionResponseDTO;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.FunctionMapper;
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
public class UpdateFunctionCommandHandler
        implements CommandHandler<UpdateFunctionCommand, ResponseEntity<FunctionResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFunctionCommandHandler.class);

    private final FunctionRepository functionRepository;
    private final FunctionMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<FunctionResponseDTO> handle(UpdateFunctionCommand command) {
        var id = FunctionId.from(command.getFunctionId());
        var dto = command.getRequest();

        var function = functionRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Função não encontrada: " + command.getFunctionId()));

        if (functionRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe uma função com code='" + dto.getCode() + "'.");
        }

        function.atualizar(dto.getCode(), dto.getName(), dto.getDescription());
        var updated = functionRepository.save(function);

        return ResponseEntity.ok(mapper.toDTO(updated));
    }
}

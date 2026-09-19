package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DesativarFunctionCommandHandler
        implements CommandHandler<DesativarFunctionCommand, ResponseEntity<SuccessResponseDTO>> {

    private final FunctionRepository functionRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(DesativarFunctionCommand command) {
        var id = FunctionId.from(command.getFunctionId());

        var function = functionRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Função não encontrada: " + command.getFunctionId()));

        function.desativar();
        functionRepository.save(function);

        return ResponseEntity.ok(SuccessResponseDTO.de(command.getFunctionId()));
    }
}

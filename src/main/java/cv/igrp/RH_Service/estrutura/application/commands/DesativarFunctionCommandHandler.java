package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DesativarFunctionCommandHandler
        implements CommandHandler<DesativarFunctionCommand, ResponseEntity<Map<String, ?>>> {

    private final FunctionRepository functionRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarFunctionCommand command) {
        var id = FunctionId.from(command.getFunctionId());

        var function = functionRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Função não encontrada: " + command.getFunctionId()));

        function.desativar();
        functionRepository.save(function);

        return ResponseEntity.ok(Map.of("message", "Desactivado com sucesso"));
    }
}

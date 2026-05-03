package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.FormacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FormacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsRemoverFormacaoCommandHandler")
@RequiredArgsConstructor
public class RemoverFormacaoCommandHandler
        implements CommandHandler<RemoverFormacaoCommand, ResponseEntity<Map<String, ?>>> {

    private final FormacaoRepository formacaoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(RemoverFormacaoCommand command) {
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        var formacaoId = FormacaoId.from(command.getFormacaoId());

        var formacao = formacaoRepository.findById(formacaoId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Formação não encontrada: " + command.getFormacaoId()));

        if (!formacao.getFuncionarioId().equals(funcionarioId))
            throw IgrpResponseStatusException.notFound("Formação não encontrada: " + command.getFormacaoId());

        formacaoRepository.deleteById(formacaoId);
        return ResponseEntity.ok(Map.of("message", "Formação removida com sucesso"));
    }
}

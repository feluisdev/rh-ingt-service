package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ColocacaoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsDesativarColocacaoCommandHandler")
@RequiredArgsConstructor
public class DesativarColocacaoCommandHandler
        implements CommandHandler<DesativarColocacaoCommand, ResponseEntity<Map<String, ?>>> {

    private final ColocacaoRepository colocacaoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarColocacaoCommand command) {
        var colocacao = colocacaoRepository.findById(ColocacaoId.from(command.getColocacaoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Colocação não encontrada: " + command.getColocacaoId()));

        if (Boolean.TRUE.equals(colocacao.getIsCurrent()))
            throw IgrpResponseStatusException.badRequest(
                    "Não é possível remover a colocação actual. Registe uma nova colocação para substituí-la.");

        colocacao.desativar();
        colocacaoRepository.save(colocacao);
        return ResponseEntity.ok(Map.of("message", "Colocação removida com sucesso"));
    }
}

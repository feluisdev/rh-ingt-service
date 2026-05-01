package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.QualificacaoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsDesativarQualificacaoCommandHandler")
@RequiredArgsConstructor
public class DesativarQualificacaoCommandHandler
        implements CommandHandler<DesativarQualificacaoCommand, ResponseEntity<Map<String, ?>>> {

    private final QualificacaoRepository qualificacaoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarQualificacaoCommand command) {
        var id = QualificacaoId.from(command.getQualificacaoId());
        var qualificacao = qualificacaoRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Qualificação não encontrada: " + command.getQualificacaoId()));

        if (Boolean.FALSE.equals(qualificacao.getIsActive()))
            throw IgrpResponseStatusException.badRequest("A qualificação já está inactiva.");

        qualificacao.desativar();
        qualificacaoRepository.save(qualificacao);
        return ResponseEntity.ok(Map.of("message", "Desactivado com sucesso"));
    }
}

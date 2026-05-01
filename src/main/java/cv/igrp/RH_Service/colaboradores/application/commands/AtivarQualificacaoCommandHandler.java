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

@Component("colabsAtivarQualificacaoCommandHandler")
@RequiredArgsConstructor
public class AtivarQualificacaoCommandHandler
        implements CommandHandler<AtivarQualificacaoCommand, ResponseEntity<Map<String, ?>>> {

    private final QualificacaoRepository qualificacaoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AtivarQualificacaoCommand command) {
        var id = QualificacaoId.from(command.getQualificacaoId());
        var qualificacao = qualificacaoRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Qualificação não encontrada: " + command.getQualificacaoId()));

        if (Boolean.TRUE.equals(qualificacao.getIsActive()))
            return ResponseEntity.ok(Map.of("message", "Qualificação já está activa."));

        qualificacao.ativar();
        qualificacaoRepository.save(qualificacao);
        return ResponseEntity.ok(Map.of("message", "Activado com sucesso"));
    }
}

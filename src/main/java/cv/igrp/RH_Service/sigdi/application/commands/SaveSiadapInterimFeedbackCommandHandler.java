package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapInterimFeedbackDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapInterimFeedback;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapInterimFeedbackRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapInterimFeedbackMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SaveSiadapInterimFeedbackCommandHandler
    implements CommandHandler<SaveSiadapInterimFeedbackCommand, ResponseEntity<SiadapInterimFeedbackDTO>> {

    private final SiadapInterimFeedbackRepository repository;
    private final SiadapEvaluationRepository evaluationRepository;
    private final SiadapInterimFeedbackMapper mapper;

    @IgrpCommandHandler
    @Transactional
    @Override
    public ResponseEntity<SiadapInterimFeedbackDTO> handle(SaveSiadapInterimFeedbackCommand command) {
        UUID evalUuid = UUID.fromString(command.getEvaluationId());
        SiadapEvaluationId evalId = SiadapEvaluationId.from(evalUuid);

        // Ensure evaluation exists
        SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação de desempenho não encontrada"));

        SiadapInterimFeedbackDTO body = command.getBody();
        body.setEvaluationId(command.getEvaluationId());

        SiadapInterimFeedback domainModel = mapper.toDomain(body);
        SiadapInterimFeedback saved = repository.save(domainModel);

        return ResponseEntity.ok(mapper.toDto(saved));
    }
}

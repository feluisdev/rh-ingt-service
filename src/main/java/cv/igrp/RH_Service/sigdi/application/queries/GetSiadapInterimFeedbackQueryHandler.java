package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.SiadapInterimFeedbackDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapInterimFeedback;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapInterimFeedbackRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapInterimFeedbackMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetSiadapInterimFeedbackQueryHandler
    implements QueryHandler<GetSiadapInterimFeedbackQuery, ResponseEntity<SiadapInterimFeedbackDTO>> {

    private final SiadapInterimFeedbackRepository repository;
    private final SiadapInterimFeedbackMapper mapper;

    @IgrpQueryHandler
    @Transactional(readOnly = true)
    @Override
    public ResponseEntity<SiadapInterimFeedbackDTO> handle(GetSiadapInterimFeedbackQuery query) {
        UUID evalUuid = UUID.fromString(query.getEvaluationId());

        SiadapInterimFeedback feedback = repository.findByEvaluationId(evalUuid)
                .orElse(null);

        if (feedback == null) {
            SiadapInterimFeedbackDTO emptyDto = new SiadapInterimFeedbackDTO();
            emptyDto.setEvaluationId(query.getEvaluationId());
            emptyDto.setCompetencyObservations(new ArrayList<>());
            emptyDto.setImprovementActions(new ArrayList<>());
            emptyDto.setObjectiveRevisions(new ArrayList<>());
            return ResponseEntity.ok(emptyDto);
        }

        return ResponseEntity.ok(mapper.toDto(feedback));
    }
}

package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.RH_Service.carreiras.application.dto.GradeResponseDTO;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.GradeMapper;
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
public class UpdateGradeCommandHandler
        implements CommandHandler<UpdateGradeCommand, ResponseEntity<GradeResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateGradeCommandHandler.class);

    private final GradeRepository gradeRepository;
    private final GradeMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<GradeResponseDTO> handle(UpdateGradeCommand command) {
        var id = GradeId.from(command.getGradeId());
        var dto = command.getRequest();

        var grade = gradeRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Escalão não encontrado: " + command.getGradeId()));

        // Reject attempts to change categoryId or gradeNumber
        if (dto.getCategoryId() != null &&
                !dto.getCategoryId().equals(grade.getCategoryId().getStringValor())) {
            throw IgrpResponseStatusException.badRequest(
                    "O campo categoryId é imutável e não pode ser alterado.");
        }
        if (dto.getGradeNumber() != null && !dto.getGradeNumber().equals(grade.getGradeNumber())) {
            throw IgrpResponseStatusException.badRequest(
                    "O campo gradeNumber é imutável e não pode ser alterado.");
        }

        grade.atualizar(dto.getName(), dto.getSalaryIndex(), dto.getSalaryBase());
        var updated = gradeRepository.save(grade);

        return ResponseEntity.ok(mapper.toDTO(updated));
    }
}

package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CreateGradeCommandHandler
        implements CommandHandler<CreateGradeCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateGradeCommandHandler.class);

    private final GradeRepository gradeRepository;
    private final CategoryRepository categoryRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateGradeCommand command) {
        var dto = command.getRequest();
        if (dto.getCategoryId() == null || dto.getCategoryId().isBlank()) {
            throw IgrpResponseStatusException.badRequest("O campo categoryId é obrigatório.");
        }
        if (dto.getGradeNumber() == null) {
            throw IgrpResponseStatusException.badRequest("O campo gradeNumber é obrigatório.");
        }
        var categoryId = CategoryId.from(dto.getCategoryId());

        categoryRepository.findById(categoryId)
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Categoria não encontrada ou inactiva: " + dto.getCategoryId()));

        if (gradeRepository.existsByGradeNumberAndCategoryId(dto.getGradeNumber(), categoryId)) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe um escalão número " + dto.getGradeNumber() + " nesta categoria.");
        }

        Grade saved = gradeRepository.save(
                Grade.criar(categoryId, dto.getGradeNumber(), dto.getName(),
                        dto.getSalaryIndex(), dto.getSalaryBase()));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}

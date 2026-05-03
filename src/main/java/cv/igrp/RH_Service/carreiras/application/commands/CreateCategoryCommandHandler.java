package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.RH_Service.carreiras.domain.models.Category;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
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
public class CreateCategoryCommandHandler
        implements CommandHandler<CreateCategoryCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateCategoryCommandHandler.class);

    private final CategoryRepository categoryRepository;
    private final CareerRepository careerRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateCategoryCommand command) {
        var dto = command.getRequest();
        if (dto.getCareerId() == null || dto.getCareerId().isBlank()) {
            throw IgrpResponseStatusException.badRequest("O campo careerId é obrigatório.");
        }
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw IgrpResponseStatusException.badRequest("O campo code é obrigatório.");
        }
        var careerId = CareerId.from(dto.getCareerId());

        careerRepository.findById(careerId)
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Carreira não encontrada ou inactiva: " + dto.getCareerId()));

        if (categoryRepository.existsByCodeAndCareerId(dto.getCode(), careerId)) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe uma categoria com code='" + dto.getCode() + "' nesta carreira.");
        }

        Category saved = categoryRepository.save(
                Category.criar(careerId, dto.getCode(), dto.getName(), dto.getDescription()));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}

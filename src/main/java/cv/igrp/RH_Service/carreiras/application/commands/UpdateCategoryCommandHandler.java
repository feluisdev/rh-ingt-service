package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.RH_Service.carreiras.application.dto.CategoryResponseDTO;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.CategoryMapper;
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
public class UpdateCategoryCommandHandler
        implements CommandHandler<UpdateCategoryCommand, ResponseEntity<CategoryResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCategoryCommandHandler.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<CategoryResponseDTO> handle(UpdateCategoryCommand command) {
        var id = CategoryId.from(command.getCategoryId());
        var dto = command.getRequest();

        var category = categoryRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Categoria não encontrada: " + command.getCategoryId()));

        // Reject attempts to change careerId or code
        if (dto.getCareerId() != null &&
                !dto.getCareerId().equals(category.getCareerId().getStringValor())) {
            throw IgrpResponseStatusException.badRequest(
                    "O campo careerId é imutável e não pode ser alterado.");
        }
        if (dto.getCode() != null && !dto.getCode().equals(category.getCode())) {
            throw IgrpResponseStatusException.badRequest(
                    "O campo code é imutável e não pode ser alterado.");
        }

        category.atualizar(dto.getName(), dto.getDescription());
        var updated = categoryRepository.save(category);

        return ResponseEntity.ok(mapper.toDTO(updated));
    }
}

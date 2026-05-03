package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
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
public class DesativarCategoryCommandHandler
        implements CommandHandler<DesativarCategoryCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesativarCategoryCommandHandler.class);

    private final CategoryRepository categoryRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarCategoryCommand command) {
        var id = CategoryId.from(command.getCategoryId());

        var category = categoryRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Categoria não encontrada: " + command.getCategoryId()));

        if (categoryRepository.existsActiveGradesByCategoryId(id)) {
            throw IgrpResponseStatusException.conflict(
                    "Não é possível desactivar a categoria: tem escalões activos.");
        }

        category.desativar();
        categoryRepository.save(category);

        return ResponseEntity.ok(Map.of("message", "Desactivado com sucesso"));
    }
}

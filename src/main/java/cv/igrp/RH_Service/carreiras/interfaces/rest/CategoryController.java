/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.carreiras.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.carreiras.application.commands.*;
import cv.igrp.RH_Service.carreiras.application.queries.*;
import cv.igrp.RH_Service.carreiras.application.dto.WrapperListaCategoryDTO;
import cv.igrp.RH_Service.carreiras.application.dto.WrapperListaGradeDTO;
import cv.igrp.RH_Service.carreiras.application.dto.CategoryResponseDTO;
import cv.igrp.RH_Service.carreiras.application.dto.CategoryRequestDTO;

import java.util.Map;
import java.util.List;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/categories")
@Tag(name = "Category", description = "Gestão de categorias profissionais (PCFR)")
public class CategoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public CategoryController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar categorias",
        description = "Retorna a lista paginada de categorias, com filtro opcional por careerId",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaCategoryDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WrapperListaCategoryDTO> getCategories(
        @RequestParam(value = "careerId", required = false) String careerId,
        @RequestParam(value = "active", required = false) Boolean active,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho,
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "nome", required = false) String nome) {

        LOGGER.debug("Operation started");

        final var query = new GetCategoriesQuery(careerId, active, pagina, tamanho, code, nome);
        ResponseEntity<WrapperListaCategoryDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{categoryId}")
    @Operation(
        summary = "Obter categoria por ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CategoryResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
        @PathVariable(value = "categoryId") String categoryId) {

        LOGGER.debug("Operation started");

        final var query = new GetCategoryByIdQuery(categoryId);
        ResponseEntity<CategoryResponseDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar categoria",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createCategory(
        @Valid @RequestBody CategoryRequestDTO createCategoryRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreateCategoryCommand(createCategoryRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{categoryId}")
    @Operation(
        summary = "Actualizar categoria",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CategoryResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<CategoryResponseDTO> updateCategory(
        @Valid @RequestBody CategoryRequestDTO updateCategoryRequest,
        @PathVariable(value = "categoryId") String categoryId) {

        LOGGER.debug("Operation started");

        final var command = new UpdateCategoryCommand(updateCategoryRequest, categoryId);
        ResponseEntity<CategoryResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping("{categoryId}")
    @Operation(
        summary = "Desactivar categoria (soft delete)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> deactivateCategory(
        @PathVariable(value = "categoryId") String categoryId) {

        LOGGER.debug("Operation started");

        final var command = new DesativarCategoryCommand(categoryId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{categoryId}/grades")
    @Operation(
        summary = "Listar escalões de uma categoria",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaGradeDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WrapperListaGradeDTO> getGradesByCategory(
        @PathVariable(value = "categoryId") String categoryId) {

        LOGGER.debug("Operation started");

        final var query = new GetGradesByCategoryIdQuery(categoryId);
        ResponseEntity<WrapperListaGradeDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{categoryId}/activate")
    @Operation(
        summary = "Reactivar categoria",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activateCategory(
        @PathVariable(value = "categoryId") String categoryId) {

        LOGGER.debug("Operation started");

        final var command = new AtivarCategoryCommand(categoryId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("combobox")
    @Operation(
        summary = "Listar para combobox",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<List<ComboboxItemDTO>> getCombobox(
        @RequestParam(value = "careerId", required = false) String careerId) {
        LOGGER.debug("Operation started");
        final var query = new GetCategoriesComboboxQuery(careerId);
        ResponseEntity<List<ComboboxItemDTO>> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}
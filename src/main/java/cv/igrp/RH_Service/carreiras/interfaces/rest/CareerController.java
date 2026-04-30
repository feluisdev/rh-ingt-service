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
import cv.igrp.RH_Service.carreiras.application.dto.WrapperListaCareerDTO;
import cv.igrp.RH_Service.carreiras.application.dto.WrapperListaCategoryDTO;
import cv.igrp.RH_Service.carreiras.application.dto.CareerResponse;
import cv.igrp.RH_Service.carreiras.application.dto.CareerRequest;

import java.util.Map;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/careers")
@Tag(name = "Career", description = "Gestão de carreiras (PCFR)")
public class CareerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CareerController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public CareerController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar carreiras",
        description = "Retorna a lista paginada de carreiras",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaCareerDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WrapperListaCareerDTO> getCareers(
        @RequestParam(value = "active", required = false) Boolean active,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho) {

        LOGGER.debug("Operation started");

        final var query = new GetCareersQuery(active, pagina, tamanho);
        ResponseEntity<WrapperListaCareerDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{careerId}")
    @Operation(
        summary = "Obter carreira por ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CareerResponse.class)
                )
            )
        }
    )
    public ResponseEntity<CareerResponse> getCareerById(
        @PathVariable(value = "careerId") String careerId) {

        LOGGER.debug("Operation started");

        final var query = new GetCareerByIdQuery(careerId);
        ResponseEntity<CareerResponse> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar carreira",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createCareer(
        @Valid @RequestBody CareerRequest createCareerRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreateCareerCommand(createCareerRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{careerId}")
    @Operation(
        summary = "Actualizar carreira",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CareerResponse.class)
                )
            )
        }
    )
    public ResponseEntity<CareerResponse> updateCareer(
        @Valid @RequestBody CareerRequest updateCareerRequest,
        @PathVariable(value = "careerId") String careerId) {

        LOGGER.debug("Operation started");

        final var command = new UpdateCareerCommand(updateCareerRequest, careerId);
        ResponseEntity<CareerResponse> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping("{careerId}")
    @Operation(
        summary = "Desactivar carreira (soft delete)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> deactivateCareer(
        @PathVariable(value = "careerId") String careerId) {

        LOGGER.debug("Operation started");

        final var command = new DesativarCareerCommand(careerId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{careerId}/categories")
    @Operation(
        summary = "Listar categorias de uma carreira",
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
    public ResponseEntity<WrapperListaCategoryDTO> getCategoriesByCareer(
        @PathVariable(value = "careerId") String careerId) {

        LOGGER.debug("Operation started");

        final var query = new GetCategoriesByCareerIdQuery(careerId);
        ResponseEntity<WrapperListaCategoryDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{careerId}/activate")
    @Operation(
        summary = "Reactivar carreira",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activateCareer(
        @PathVariable(value = "careerId") String careerId) {

        LOGGER.debug("Operation started");

        final var command = new AtivarCareerCommand(careerId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}

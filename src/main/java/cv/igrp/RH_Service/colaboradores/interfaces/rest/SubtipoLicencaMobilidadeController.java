package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.*;
import cv.igrp.RH_Service.colaboradores.application.dto.SubtipoLicencaMobilidadeRequest;
import cv.igrp.RH_Service.colaboradores.application.dto.SubtipoLicencaMobilidadeResponse;
import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaSubtipoLicencaMobilidadeDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.*;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.framework.stereotype.IgrpController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@IgrpController
@RestController("colabsSubtipoLicencaMobilidadeController")
@RequestMapping(path = "api/v1/rh/parametrizacoes/subtipos-licenca-mobilidade")
@Tag(name = "SubtipoLicencaMobilidade", description = "Gestão de subtipos de licença e mobilidade")
public class SubtipoLicencaMobilidadeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubtipoLicencaMobilidadeController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public SubtipoLicencaMobilidadeController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(summary = "Criar subtipo de licença/mobilidade")
    public ResponseEntity<Map<String, ?>> create(@Valid @RequestBody SubtipoLicencaMobilidadeRequest request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new CreateSubtipoLicencaMobilidadeCommand(request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping
    @Operation(summary = "Listar subtipos de licença/mobilidade")
    public ResponseEntity<WrapperListaSubtipoLicencaMobilidadeDTO> getAll(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String recordType) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaSubtipoLicencaMobilidadeDTO> response = queryBus.handle(new GetSubtiposLicencaMobilidadeQuery(active, recordType));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{id}")
    @Operation(summary = "Obter subtipo por ID")
    public ResponseEntity<SubtipoLicencaMobilidadeResponse> getById(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<SubtipoLicencaMobilidadeResponse> response = queryBus.handle(new GetSubtipoLicencaMobilidadeByIdQuery(id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{id}")
    @Operation(summary = "Actualizar subtipo")
    public ResponseEntity<Map<String, ?>> update(@Valid @RequestBody SubtipoLicencaMobilidadeRequest request, @PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new UpdateSubtipoLicencaMobilidadeCommand(request, id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{id}/ativar")
    @Operation(summary = "Activar subtipo")
    public ResponseEntity<Map<String, ?>> ativar(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new AtivarSubtipoLicencaMobilidadeCommand(id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{id}/desativar")
    @Operation(summary = "Desactivar subtipo")
    public ResponseEntity<Map<String, ?>> desativar(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new DesativarSubtipoLicencaMobilidadeCommand(id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}

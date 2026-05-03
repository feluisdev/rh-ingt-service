/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.*;
import cv.igrp.RH_Service.colaboradores.application.dto.*;
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
@RestController("colabsReciboController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "Recibos de Vencimento", description = "Gestão de recibos de vencimento dos funcionários")
public class ReciboController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReciboController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ReciboController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("funcionarios/{funcionarioId}/recibos")
    @Operation(summary = "Listar recibos de vencimento do funcionário")
    public ResponseEntity<WrapperListaReciboDTO> listarRecibos(
            @PathVariable String funcionarioId,
            @RequestParam(required = false) Integer periodYear,
            @RequestParam(required = false) Integer periodMonth) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaReciboDTO> response = queryBus.handle(
                new GetRecibosByFuncionarioQuery(funcionarioId, periodYear, periodMonth));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("recibos/{reciboId}")
    @Operation(summary = "Obter recibo por ID")
    public ResponseEntity<ReciboVencimentoDTO> getReciboById(@PathVariable String reciboId) {
        LOGGER.debug("Operation started");
        ResponseEntity<ReciboVencimentoDTO> response = queryBus.handle(new GetReciboVencimentoQuery(reciboId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("funcionarios/{funcionarioId}/recibos")
    @Operation(summary = "Emitir recibo de vencimento")
    public ResponseEntity<Map<String, ?>> criarRecibo(
            @PathVariable String funcionarioId,
            @Valid @RequestBody CriarReciboRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new CriarReciboVencimentoCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}

/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AccaoCartaoProfissionalCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.EmitirCartaoProfissionalCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.CartaoProfissionalDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.CartaoProfissionalRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetCartoesProfissionaisQuery;
import cv.igrp.framework.core.domain.Command;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.Query;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.framework.stereotype.IgrpController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@IgrpController
@RestController("colabsCartaoProfissionalController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "CartaoProfissional", description = "Cartão de identificação profissional (Lei n.º 20/X/2023, art. 25.º): emissão em PDF (modelo de teste), entrega atestada, devolução, anulação e validade pela função e categoria")
public class CartaoProfissionalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartaoProfissionalController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public CartaoProfissionalController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping("funcionarios/{funcionarioId}/cartao-profissional")
    @Operation(summary = "Emitir um cartão novo (o que estiver em uso fica anulado, substituído)")
    @ApiResponse(responseCode = "201", description = "Cartão emitido",
            content = @Content(schema = @Schema(implementation = CartaoProfissionalDTO.class)))
    public ResponseEntity<CartaoProfissionalDTO> emitirCartaoProfissional(@PathVariable String funcionarioId) {
        return enviar(new EmitirCartaoProfissionalCommand(funcionarioId));
    }

    @GetMapping("funcionarios/{funcionarioId}/cartao-profissional")
    @Operation(summary = "Os cartões do colaborador, do mais recente para o mais antigo, cada um com a validade de hoje")
    @ApiResponse(responseCode = "200", description = "Cartões",
            content = @Content(schema = @Schema(implementation = CartaoProfissionalDTO.class)))
    public ResponseEntity<List<CartaoProfissionalDTO>> getCartoesProfissionais(@PathVariable String funcionarioId) {
        return perguntar(new GetCartoesProfissionaisQuery(funcionarioId));
    }

    @PatchMapping("funcionarios/{funcionarioId}/cartao-profissional/{cartaoId}/entregar")
    @Operation(summary = "Registar a entrega do cartão (o colaborador atesta a recepção)")
    @ApiResponse(responseCode = "200", description = "Entregue",
            content = @Content(schema = @Schema(implementation = CartaoProfissionalDTO.class)))
    public ResponseEntity<CartaoProfissionalDTO> entregarCartaoProfissional(@PathVariable String funcionarioId, @PathVariable String cartaoId,
                                                                            @RequestBody CartaoProfissionalRequestDTO request) {
        return enviar(new AccaoCartaoProfissionalCommand(funcionarioId, cartaoId, "ENTREGAR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/cartao-profissional/{cartaoId}/devolver")
    @Operation(summary = "Registar a devolução do cartão (saída, mudança de categoria ou de função)")
    @ApiResponse(responseCode = "200", description = "Devolvido",
            content = @Content(schema = @Schema(implementation = CartaoProfissionalDTO.class)))
    public ResponseEntity<CartaoProfissionalDTO> devolverCartaoProfissional(@PathVariable String funcionarioId, @PathVariable String cartaoId,
                                                                            @RequestBody CartaoProfissionalRequestDTO request) {
        return enviar(new AccaoCartaoProfissionalCommand(funcionarioId, cartaoId, "DEVOLVER", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/cartao-profissional/{cartaoId}/anular")
    @Operation(summary = "Anular o cartão (perda, extravio), com motivo")
    @ApiResponse(responseCode = "200", description = "Anulado",
            content = @Content(schema = @Schema(implementation = CartaoProfissionalDTO.class)))
    public ResponseEntity<CartaoProfissionalDTO> anularCartaoProfissional(@PathVariable String funcionarioId, @PathVariable String cartaoId,
                                                                          @RequestBody CartaoProfissionalRequestDTO request) {
        return enviar(new AccaoCartaoProfissionalCommand(funcionarioId, cartaoId, "ANULAR", request));
    }

    @GetMapping("me/cartao-profissional")
    @Operation(summary = "Os meus cartões profissionais")
    @ApiResponse(responseCode = "200", description = "Cartões",
            content = @Content(schema = @Schema(implementation = CartaoProfissionalDTO.class)))
    public ResponseEntity<List<CartaoProfissionalDTO>> getMeusCartoesProfissionais() {
        return perguntar(new GetCartoesProfissionaisQuery(null));
    }

    private <T> ResponseEntity<T> enviar(Command command) {
        LOGGER.debug("Operation started");
        ResponseEntity<T> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    private <T> ResponseEntity<T> perguntar(Query query) {
        LOGGER.debug("Operation started");
        ResponseEntity<T> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}

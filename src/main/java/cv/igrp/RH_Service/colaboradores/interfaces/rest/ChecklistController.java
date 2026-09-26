/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AbrirChecklistCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.AccaoChecklistCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.GuardarItemChecklistModeloCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.MarcarItemChecklistCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.ChecklistDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ChecklistRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ItemChecklistModeloDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ItemChecklistModeloRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ItemChecklistRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetChecklistsPendentesQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetChecklistsQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetModeloChecklistQuery;
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
@RestController("colabsChecklistController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "Checklist", description = "Checklists de entrada (acolhimento e integração, Lei n.º 20/X/2023, art. 141.º n.º 2) e de saída (entregas e passagem de serviço): modelo parametrizável, abertura pela admissão e pela cessação, itens por área e marcação automática")
public class ChecklistController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChecklistController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ChecklistController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("checklists/modelo")
    @Operation(summary = "O modelo das checklists (filtro opcional por tipo)")
    @ApiResponse(responseCode = "200", description = "Itens do modelo",
            content = @Content(schema = @Schema(implementation = ItemChecklistModeloDTO.class)))
    public ResponseEntity<List<ItemChecklistModeloDTO>> getModeloChecklist(@RequestParam(value = "tipo", required = false) String tipo) {
        return perguntar(new GetModeloChecklistQuery(tipo));
    }

    @PostMapping("checklists/modelo")
    @Operation(summary = "Acrescentar um item ao modelo (entra nas checklists abertas daqui em diante)")
    @ApiResponse(responseCode = "201", description = "Criado",
            content = @Content(schema = @Schema(implementation = ItemChecklistModeloDTO.class)))
    public ResponseEntity<ItemChecklistModeloDTO> criarItemChecklistModelo(@RequestBody ItemChecklistModeloRequestDTO request) {
        return enviar(new GuardarItemChecklistModeloCommand(null, request));
    }

    @PutMapping("checklists/modelo/{itemId}")
    @Operation(summary = "Alterar um item do modelo, ou desactivá-lo (campos omitidos ficam como estão)")
    @ApiResponse(responseCode = "200", description = "Alterado",
            content = @Content(schema = @Schema(implementation = ItemChecklistModeloDTO.class)))
    public ResponseEntity<ItemChecklistModeloDTO> actualizarItemChecklistModelo(@PathVariable String itemId,
                                                                                @RequestBody ItemChecklistModeloRequestDTO request) {
        return enviar(new GuardarItemChecklistModeloCommand(itemId, request));
    }

    @GetMapping("checklists")
    @Operation(summary = "A lista de trabalho: checklists abertas com itens pendentes (por tipo, por área responsável, só as atrasadas)")
    @ApiResponse(responseCode = "200", description = "Checklists",
            content = @Content(schema = @Schema(implementation = ChecklistDTO.class)))
    public ResponseEntity<List<ChecklistDTO>> getChecklistsPendentes(@RequestParam(value = "tipo", required = false) String tipo,
                                                                     @RequestParam(value = "responsavel", required = false) String responsavel,
                                                                     @RequestParam(value = "atrasadas", required = false) Boolean atrasadas) {
        return perguntar(new GetChecklistsPendentesQuery(tipo, responsavel, atrasadas));
    }

    @GetMapping("funcionarios/{funcionarioId}/checklists")
    @Operation(summary = "As checklists de entrada e saída do colaborador, da mais recente para a mais antiga")
    @ApiResponse(responseCode = "200", description = "Checklists",
            content = @Content(schema = @Schema(implementation = ChecklistDTO.class)))
    public ResponseEntity<List<ChecklistDTO>> getChecklists(@PathVariable String funcionarioId) {
        return perguntar(new GetChecklistsQuery(funcionarioId));
    }

    @PostMapping("funcionarios/{funcionarioId}/checklists")
    @Operation(summary = "Abrir uma checklist à mão (a admissão e a cessação já a abrem sozinhas)")
    @ApiResponse(responseCode = "201", description = "Aberta",
            content = @Content(schema = @Schema(implementation = ChecklistDTO.class)))
    public ResponseEntity<ChecklistDTO> abrirChecklist(@PathVariable String funcionarioId, @RequestBody ChecklistRequestDTO request) {
        return enviar(new AbrirChecklistCommand(funcionarioId, request));
    }

    @PostMapping("funcionarios/{funcionarioId}/checklists/{checklistId}/itens")
    @Operation(summary = "Acrescentar um item só a esta checklist")
    @ApiResponse(responseCode = "200", description = "Acrescentado",
            content = @Content(schema = @Schema(implementation = ChecklistDTO.class)))
    public ResponseEntity<ChecklistDTO> acrescentarItemChecklist(@PathVariable String funcionarioId, @PathVariable String checklistId,
                                                                @RequestBody ChecklistRequestDTO request) {
        return enviar(new AccaoChecklistCommand(funcionarioId, checklistId, "ACRESCENTAR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/checklists/{checklistId}/itens/{itemId}")
    @Operation(summary = "Marcar um item: feito, não aplicável (num obrigatório, com a razão) ou de volta a pendente")
    @ApiResponse(responseCode = "200", description = "Marcado",
            content = @Content(schema = @Schema(implementation = ChecklistDTO.class)))
    public ResponseEntity<ChecklistDTO> marcarItemChecklist(@PathVariable String funcionarioId, @PathVariable String checklistId,
                                                           @PathVariable String itemId, @RequestBody ItemChecklistRequestDTO request) {
        return enviar(new MarcarItemChecklistCommand(funcionarioId, checklistId, itemId, request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/checklists/{checklistId}/cancelar")
    @Operation(summary = "Cancelar a checklist, com motivo")
    @ApiResponse(responseCode = "200", description = "Cancelada",
            content = @Content(schema = @Schema(implementation = ChecklistDTO.class)))
    public ResponseEntity<ChecklistDTO> cancelarChecklist(@PathVariable String funcionarioId, @PathVariable String checklistId,
                                                         @RequestBody ChecklistRequestDTO request) {
        return enviar(new AccaoChecklistCommand(funcionarioId, checklistId, "CANCELAR", request));
    }

    @GetMapping("me/checklists")
    @Operation(summary = "As minhas checklists e as da minha equipa directa com itens da chefia por tratar")
    @ApiResponse(responseCode = "200", description = "Checklists",
            content = @Content(schema = @Schema(implementation = ChecklistDTO.class)))
    public ResponseEntity<List<ChecklistDTO>> getMinhasChecklists() {
        return perguntar(new GetChecklistsQuery(null));
    }

    @PatchMapping("me/checklists/{checklistId}/itens/{itemId}")
    @Operation(summary = "Marcar um item meu (do próprio) ou da chefia (da minha equipa directa)")
    @ApiResponse(responseCode = "200", description = "Marcado",
            content = @Content(schema = @Schema(implementation = ChecklistDTO.class)))
    public ResponseEntity<ChecklistDTO> marcarMeuItemChecklist(@PathVariable String checklistId, @PathVariable String itemId,
                                                              @RequestBody ItemChecklistRequestDTO request) {
        return enviar(new MarcarItemChecklistCommand(null, checklistId, itemId, request));
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

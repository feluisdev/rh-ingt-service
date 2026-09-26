/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AccaoAcidenteServicoCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.AcidenteServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.AcidenteServicoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetAcidentesServicoQuery;
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
@RestController("colabsAcidenteServicoController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "AcidenteServico", description = "Acidentes em serviço e doenças profissionais (Lei n.º 20/X/2023, arts. 187.º–191.º): participação, qualificação, incapacidades, alta, incapacidade permanente, seguradora")
public class AcidenteServicoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcidenteServicoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public AcidenteServicoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("acidentes-servico")
    @Operation(summary = "Os acidentes em serviço e doenças profissionais (filtro: estado), com alertas")
    @ApiResponse(responseCode = "200", description = "Acidentes",
            content = @Content(schema = @Schema(implementation = AcidenteServicoDTO.class)))
    public ResponseEntity<List<AcidenteServicoDTO>> getAcidentesServico(@RequestParam(value = "estado", required = false) String estado) {
        return perguntar(new GetAcidentesServicoQuery(estado, null, false));
    }

    @GetMapping("funcionarios/{funcionarioId}/acidentes-servico")
    @Operation(summary = "Os acidentes do colaborador")
    @ApiResponse(responseCode = "200", description = "Acidentes",
            content = @Content(schema = @Schema(implementation = AcidenteServicoDTO.class)))
    public ResponseEntity<List<AcidenteServicoDTO>> getAcidentesServicoColaborador(@PathVariable String funcionarioId) {
        return perguntar(new GetAcidentesServicoQuery(null, funcionarioId, false));
    }

    @GetMapping("me/acidentes-servico")
    @Operation(summary = "Os meus acidentes em serviço")
    @ApiResponse(responseCode = "200", description = "Acidentes",
            content = @Content(schema = @Schema(implementation = AcidenteServicoDTO.class)))
    public ResponseEntity<List<AcidenteServicoDTO>> getMeusAcidentesServico() {
        return perguntar(new GetAcidentesServicoQuery(null, null, true));
    }

    @PostMapping("funcionarios/{funcionarioId}/acidentes-servico")
    @Operation(summary = "Participar um acidente em serviço, de trajecto ou doença profissional (pelo RH)")
    @ApiResponse(responseCode = "201", description = "Participado",
            content = @Content(schema = @Schema(implementation = AcidenteServicoDTO.class)))
    public ResponseEntity<AcidenteServicoDTO> participarAcidenteServico(@PathVariable String funcionarioId, @RequestBody AcidenteServicoRequestDTO request) {
        return enviar(new AccaoAcidenteServicoCommand(funcionarioId, null, "PARTICIPAR", request, false));
    }

    @PatchMapping("funcionarios/{funcionarioId}/acidentes-servico/{acidenteId}/qualificar")
    @Operation(summary = "Qualificar: é acidente em serviço (despacho) ou não (motivo — art. 187.º n.º 3)")
    @ApiResponse(responseCode = "200", description = "Qualificado",
            content = @Content(schema = @Schema(implementation = AcidenteServicoDTO.class)))
    public ResponseEntity<AcidenteServicoDTO> qualificarAcidenteServico(@PathVariable String funcionarioId, @PathVariable String acidenteId, @RequestBody AcidenteServicoRequestDTO request) {
        return enviar(new AccaoAcidenteServicoCommand(funcionarioId, acidenteId, "QUALIFICAR", request, false));
    }

    @PostMapping("funcionarios/{funcionarioId}/acidentes-servico/{acidenteId}/incapacidades")
    @Operation(summary = "Registar um período de incapacidade temporária (faltas justificadas sem perda de direitos — art. 188.º)")
    @ApiResponse(responseCode = "200", description = "Registada",
            content = @Content(schema = @Schema(implementation = AcidenteServicoDTO.class)))
    public ResponseEntity<AcidenteServicoDTO> registarIncapacidadeAcidenteServico(@PathVariable String funcionarioId, @PathVariable String acidenteId, @RequestBody AcidenteServicoRequestDTO request) {
        return enviar(new AccaoAcidenteServicoCommand(funcionarioId, acidenteId, "INCAPACIDADE", request, false));
    }

    @PatchMapping("funcionarios/{funcionarioId}/acidentes-servico/{acidenteId}/alta")
    @Operation(summary = "Registar a alta (fecha a incapacidade em aberto na véspera)")
    @ApiResponse(responseCode = "200", description = "Alta",
            content = @Content(schema = @Schema(implementation = AcidenteServicoDTO.class)))
    public ResponseEntity<AcidenteServicoDTO> altaAcidenteServico(@PathVariable String funcionarioId, @PathVariable String acidenteId, @RequestBody AcidenteServicoRequestDTO request) {
        return enviar(new AccaoAcidenteServicoCommand(funcionarioId, acidenteId, "ALTA", request, false));
    }

    @PatchMapping("funcionarios/{funcionarioId}/acidentes-servico/{acidenteId}/incapacidade-permanente")
    @Operation(summary = "Registar a incapacidade permanente; absoluta, ou parcial que impede as funções, abre a aposentação por invalidez (art. 189.º)")
    @ApiResponse(responseCode = "200", description = "Registada",
            content = @Content(schema = @Schema(implementation = AcidenteServicoDTO.class)))
    public ResponseEntity<AcidenteServicoDTO> incapacidadePermanenteAcidenteServico(@PathVariable String funcionarioId, @PathVariable String acidenteId, @RequestBody AcidenteServicoRequestDTO request) {
        return enviar(new AccaoAcidenteServicoCommand(funcionarioId, acidenteId, "INCAPACIDADE_PERMANENTE", request, false));
    }

    @PatchMapping("funcionarios/{funcionarioId}/acidentes-servico/{acidenteId}/seguradora")
    @Operation(summary = "Registar a seguradora, a apólice e a participação (art. 191.º)")
    @ApiResponse(responseCode = "200", description = "Registada",
            content = @Content(schema = @Schema(implementation = AcidenteServicoDTO.class)))
    public ResponseEntity<AcidenteServicoDTO> seguradoraAcidenteServico(@PathVariable String funcionarioId, @PathVariable String acidenteId, @RequestBody AcidenteServicoRequestDTO request) {
        return enviar(new AccaoAcidenteServicoCommand(funcionarioId, acidenteId, "SEGURADORA", request, false));
    }

    @PatchMapping("funcionarios/{funcionarioId}/acidentes-servico/{acidenteId}/encerrar")
    @Operation(summary = "Encerrar o processo do acidente")
    @ApiResponse(responseCode = "200", description = "Encerrado",
            content = @Content(schema = @Schema(implementation = AcidenteServicoDTO.class)))
    public ResponseEntity<AcidenteServicoDTO> encerrarAcidenteServico(@PathVariable String funcionarioId, @PathVariable String acidenteId, @RequestBody(required = false) AcidenteServicoRequestDTO request) {
        return enviar(new AccaoAcidenteServicoCommand(funcionarioId, acidenteId, "ENCERRAR", request, false));
    }

    @PostMapping("me/acidentes-servico")
    @Operation(summary = "Participar o meu acidente ou doença profissional")
    @ApiResponse(responseCode = "201", description = "Participado",
            content = @Content(schema = @Schema(implementation = AcidenteServicoDTO.class)))
    public ResponseEntity<AcidenteServicoDTO> participarMeuAcidenteServico(@RequestBody AcidenteServicoRequestDTO request) {
        return enviar(new AccaoAcidenteServicoCommand(null, null, "PARTICIPAR", request, true));
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

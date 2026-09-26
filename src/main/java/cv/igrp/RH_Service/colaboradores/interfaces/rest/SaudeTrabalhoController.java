/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AccaoJuntaMedicaCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.RegistarExameSaudeCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.ExameSaudeDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ExameSaudeRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.JuntaMedicaDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.JuntaMedicaRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetExamesSaudeQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetJuntasMedicasQuery;
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
@RestController("colabsSaudeTrabalhoController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "SaudeTrabalho", description = "Medicina do trabalho (exames de aptidão, sem dados clínicos, com validade) e comissão de verificação de incapacidade (junta médica)")
public class SaudeTrabalhoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaudeTrabalhoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public SaudeTrabalhoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("funcionarios/{funcionarioId}/exames-saude")
    @Operation(summary = "Os exames de medicina do trabalho do colaborador, do mais recente")
    @ApiResponse(responseCode = "200", description = "Exames",
            content = @Content(schema = @Schema(implementation = ExameSaudeDTO.class)))
    public ResponseEntity<List<ExameSaudeDTO>> getExamesSaude(@PathVariable String funcionarioId) {
        return perguntar(new GetExamesSaudeQuery(funcionarioId));
    }

    @PostMapping("funcionarios/{funcionarioId}/exames-saude")
    @Operation(summary = "Registar um exame (resultado de aptidão, restrições, validade)")
    @ApiResponse(responseCode = "201", description = "Registado",
            content = @Content(schema = @Schema(implementation = ExameSaudeDTO.class)))
    public ResponseEntity<ExameSaudeDTO> registarExameSaude(@PathVariable String funcionarioId, @RequestBody ExameSaudeRequestDTO request) {
        return enviar(new RegistarExameSaudeCommand(funcionarioId, request));
    }

    @GetMapping("juntas-medicas")
    @Operation(summary = "Os pedidos de junta médica (filtro: estado)")
    @ApiResponse(responseCode = "200", description = "Juntas",
            content = @Content(schema = @Schema(implementation = JuntaMedicaDTO.class)))
    public ResponseEntity<List<JuntaMedicaDTO>> getJuntasMedicas(@RequestParam(value = "estado", required = false) String estado) {
        return perguntar(new GetJuntasMedicasQuery(estado, null));
    }

    @GetMapping("funcionarios/{funcionarioId}/juntas-medicas")
    @Operation(summary = "Os pedidos de junta médica do colaborador")
    @ApiResponse(responseCode = "200", description = "Juntas",
            content = @Content(schema = @Schema(implementation = JuntaMedicaDTO.class)))
    public ResponseEntity<List<JuntaMedicaDTO>> getJuntasMedicasColaborador(@PathVariable String funcionarioId) {
        return perguntar(new GetJuntasMedicasQuery(null, funcionarioId));
    }

    @PostMapping("funcionarios/{funcionarioId}/juntas-medicas")
    @Operation(summary = "Pedir a comissão de verificação de incapacidade")
    @ApiResponse(responseCode = "201", description = "Pedida",
            content = @Content(schema = @Schema(implementation = JuntaMedicaDTO.class)))
    public ResponseEntity<JuntaMedicaDTO> pedirJuntaMedica(@PathVariable String funcionarioId, @RequestBody JuntaMedicaRequestDTO request) {
        return enviar(new AccaoJuntaMedicaCommand(funcionarioId, null, "PEDIR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/juntas-medicas/{juntaId}/parecer")
    @Operation(summary = "Registar o parecer da junta (incapaz permanente abre a aposentação por invalidez)")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = JuntaMedicaDTO.class)))
    public ResponseEntity<JuntaMedicaDTO> parecerJuntaMedica(@PathVariable String funcionarioId, @PathVariable String juntaId,
                                                            @RequestBody JuntaMedicaRequestDTO request) {
        return enviar(new AccaoJuntaMedicaCommand(funcionarioId, juntaId, "PARECER", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/juntas-medicas/{juntaId}/cancelar")
    @Operation(summary = "Cancelar o pedido de junta")
    @ApiResponse(responseCode = "200", description = "Cancelada",
            content = @Content(schema = @Schema(implementation = JuntaMedicaDTO.class)))
    public ResponseEntity<JuntaMedicaDTO> cancelarJuntaMedica(@PathVariable String funcionarioId, @PathVariable String juntaId) {
        return enviar(new AccaoJuntaMedicaCommand(funcionarioId, juntaId, "CANCELAR", null));
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

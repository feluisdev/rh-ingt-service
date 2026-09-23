/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AtribuirHorarioColaboradorCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.HorarioColaboradorRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.HorarioColaboradorResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.HorarioVigenteResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetHorarioVigenteQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.ListHorariosColaboradorQuery;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.framework.stereotype.IgrpController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@IgrpController
@RestController("colabsHorarioColaboradorController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "HorarioColaborador", description = "Horário do colaborador e regime de prestação (Lei n.º 20/X/2023, arts. 164.º a 166.º)")
public class HorarioColaboradorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HorarioColaboradorController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public HorarioColaboradorController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("funcionarios/{funcionarioId}/horarios")
    @Operation(summary = "Histórico dos horários atribuídos ao colaborador")
    @ApiResponse(responseCode = "200", description = "Histórico",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = HorarioColaboradorResponseDTO.class))))
    public ResponseEntity<List<HorarioColaboradorResponseDTO>> listHorarios(@PathVariable String funcionarioId) {
        LOGGER.debug("Operation started");
        ResponseEntity<List<HorarioColaboradorResponseDTO>> response = queryBus.handle(new ListHorariosColaboradorQuery(funcionarioId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("funcionarios/{funcionarioId}/horarios")
    @Operation(summary = "Atribuir horário ao colaborador a partir de uma data; fecha o anterior na véspera")
    @ApiResponse(responseCode = "201", description = "Horário atribuído",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> atribuirHorario(
            @PathVariable String funcionarioId, @RequestBody HorarioColaboradorRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new AtribuirHorarioColaboradorCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("funcionarios/{funcionarioId}/horarios/vigente")
    @Operation(summary = "Horário que vale numa data: do colaborador, da unidade, o base, ou nenhum")
    @ApiResponse(responseCode = "200", description = "Horário vigente",
            content = @Content(schema = @Schema(implementation = HorarioVigenteResponseDTO.class)))
    public ResponseEntity<HorarioVigenteResponseDTO> getHorarioVigente(
            @PathVariable String funcionarioId,
            @RequestParam(value = "data", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        LOGGER.debug("Operation started");
        ResponseEntity<HorarioVigenteResponseDTO> response = queryBus.handle(new GetHorarioVigenteQuery(funcionarioId, data));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}

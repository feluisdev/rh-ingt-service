/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AccaoComissaoServicoCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.ComissaoServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ComissaoServicoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetComissoesServicoQuery;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@IgrpController
@RestController("colabsComissaoServicoController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "ComissaoServico", description = "Comissão de serviço dos dirigentes e cargos de livre escolha (Lei n.º 20/X/2023, arts. 59.º, 60.º e 64.º): renovação por iguais períodos de 3 anos, cessação a todo o tempo com aviso prévio de 60 dias, factos e aviso do termo")
public class ComissaoServicoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComissaoServicoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ComissaoServicoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("comissoes-servico")
    @Operation(summary = "As comissões de serviço em curso, pelo fim (só as que terminam até uma data, se indicada)")
    @ApiResponse(responseCode = "200", description = "Comissões",
            content = @Content(schema = @Schema(implementation = ComissaoServicoDTO.class)))
    public ResponseEntity<List<ComissaoServicoDTO>> getComissoesServico(
            @RequestParam(value = "terminaAte", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate terminaAte) {
        return perguntar(new GetComissoesServicoQuery(terminaAte));
    }

    @PatchMapping("funcionarios/{funcionarioId}/comissoes-servico/{licencaId}/renovar")
    @Operation(summary = "Renovar a comissão por mais 3 anos a partir do fim actual (antes de terminar)")
    @ApiResponse(responseCode = "200", description = "Renovada",
            content = @Content(schema = @Schema(implementation = ComissaoServicoDTO.class)))
    public ResponseEntity<ComissaoServicoDTO> renovarComissaoServico(@PathVariable String funcionarioId, @PathVariable String licencaId,
                                                                    @RequestBody(required = false) ComissaoServicoRequestDTO request) {
        return enviar(new AccaoComissaoServicoCommand(funcionarioId, licencaId, "RENOVAR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/comissoes-servico/{licencaId}/cessar")
    @Operation(summary = "Cessar a comissão: pela entidade ou pelo nomeado com aviso prévio de 60 dias, ou por pena disciplinar")
    @ApiResponse(responseCode = "200", description = "Cessação registada",
            content = @Content(schema = @Schema(implementation = ComissaoServicoDTO.class)))
    public ResponseEntity<ComissaoServicoDTO> cessarComissaoServico(@PathVariable String funcionarioId, @PathVariable String licencaId,
                                                                   @RequestBody ComissaoServicoRequestDTO request) {
        return enviar(new AccaoComissaoServicoCommand(funcionarioId, licencaId, "CESSAR", request));
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

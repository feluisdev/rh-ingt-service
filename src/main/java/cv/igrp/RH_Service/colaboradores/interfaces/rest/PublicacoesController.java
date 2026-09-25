/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AccaoPublicacaoOficialCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.CriarPublicacaoOficialCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.PublicacaoOficialDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.PublicacaoOficialRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetPublicacoesOficiaisQuery;
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
@RestController("colabsPublicacoesController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "Publicacoes", description = "Actos a publicar no Boletim Oficial ou na página electrónica (Lei n.º 20/X/2023, arts. 89.º–90.º): nascem dos actos do RH, extracto em PDF e registo da publicação")
public class PublicacoesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicacoesController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public PublicacoesController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("publicacoes")
    @Operation(summary = "Os actos a publicar, por estado (por omissão, todos), dos mais antigos para os mais recentes")
    @ApiResponse(responseCode = "200", description = "Publicações",
            content = @Content(schema = @Schema(implementation = PublicacaoOficialDTO.class)))
    public ResponseEntity<List<PublicacaoOficialDTO>> getPublicacoes(@RequestParam(value = "estado", required = false) String estado) {
        return perguntar(new GetPublicacoesOficiaisQuery(estado, null));
    }

    @PostMapping("publicacoes")
    @Operation(summary = "Criar à mão um acto a publicar (os previstos na lei nascem sozinhos dos actos do RH)")
    @ApiResponse(responseCode = "201", description = "Acto a publicar",
            content = @Content(schema = @Schema(implementation = PublicacaoOficialDTO.class)))
    public ResponseEntity<PublicacaoOficialDTO> criarPublicacao(@RequestBody PublicacaoOficialRequestDTO request) {
        return enviar(new CriarPublicacaoOficialCommand(request));
    }

    @PostMapping("publicacoes/{publicacaoId}/extracto")
    @Operation(summary = "Gerar o extracto do acto (PDF numerado no MinIO, modelo de teste)")
    @ApiResponse(responseCode = "200", description = "Extracto gerado",
            content = @Content(schema = @Schema(implementation = PublicacaoOficialDTO.class)))
    public ResponseEntity<PublicacaoOficialDTO> gerarExtractoPublicacao(@PathVariable String publicacaoId) {
        return enviar(new AccaoPublicacaoOficialCommand(publicacaoId, "EXTRACTO", null));
    }

    @PatchMapping("publicacoes/{publicacaoId}/publicada")
    @Operation(summary = "Registar a publicação: série, número e data do Boletim Oficial")
    @ApiResponse(responseCode = "200", description = "Publicada",
            content = @Content(schema = @Schema(implementation = PublicacaoOficialDTO.class)))
    public ResponseEntity<PublicacaoOficialDTO> registarPublicacao(@PathVariable String publicacaoId,
                                                                   @RequestBody PublicacaoOficialRequestDTO request) {
        return enviar(new AccaoPublicacaoOficialCommand(publicacaoId, "PUBLICADA", request));
    }

    @PatchMapping("publicacoes/{publicacaoId}/cancelar")
    @Operation(summary = "Cancelar um acto a publicar, com motivo")
    @ApiResponse(responseCode = "200", description = "Cancelada",
            content = @Content(schema = @Schema(implementation = PublicacaoOficialDTO.class)))
    public ResponseEntity<PublicacaoOficialDTO> cancelarPublicacao(@PathVariable String publicacaoId,
                                                                   @RequestBody PublicacaoOficialRequestDTO request) {
        return enviar(new AccaoPublicacaoOficialCommand(publicacaoId, "CANCELAR", request));
    }

    @GetMapping("funcionarios/{funcionarioId}/publicacoes")
    @Operation(summary = "Os actos publicados (ou a publicar) de um colaborador")
    @ApiResponse(responseCode = "200", description = "Publicações",
            content = @Content(schema = @Schema(implementation = PublicacaoOficialDTO.class)))
    public ResponseEntity<List<PublicacaoOficialDTO>> getPublicacoesDoFuncionario(@PathVariable String funcionarioId) {
        return perguntar(new GetPublicacoesOficiaisQuery(null, funcionarioId));
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

package cv.igrp.RH_Service.sigdi.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.sigdi.application.queries.*;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.RH_Service.sigdi.application.commands.*;
import cv.igrp.RH_Service.sigdi.application.dto.CreatePaaSubmissionPeriodDTO;
import cv.igrp.RH_Service.sigdi.application.dto.FormGenerationDetailDTO;
import cv.igrp.RH_Service.sigdi.application.dto.FormGenerationSummaryDTO;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperListPaaSubmissionPeriodDTO;
import cv.igrp.RH_Service.shared.security.DenialMessage;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Hand-maintained controller for PAA/SIADAP submission-period management endpoints.
 *
 * These endpoints used to live inside TaticalController, which carries an iGRP Studio
 * "DO NOT MODIFY / GENERATED AUTOMATICALLY" banner. Hand-adding endpoints to a generated
 * file risks silent deletion if that controller is ever regenerated from its source spec
 * (see 59-REVIEW.md WR-02). This controller is explicitly outside the generator's
 * ownership so it survives regeneration of TaticalController.
 */
@IgrpController
@RestController
@RequestMapping(path = "api/v1/tactical")
@Tag(
    name = "SIGDI-Tatical-Periods",
    description = "Gestão de períodos de submissão PAA/SIADAP"
)
public class PaaSubmissionPeriodController {

  private final QueryBus queryBus;
  private final CommandBus commandBus;

  public PaaSubmissionPeriodController(QueryBus queryBus, CommandBus commandBus) {
      this.queryBus = queryBus;
      this.commandBus = commandBus;
  }

  // ACTOR-CHECK: ENFORCED -- paa.periodoSubmissao.criar permission (IgrpAuthorizationService);
  // the endpoint was previously guarded by hasRole("RH"), a role never confirmed against the
  // real IAM realm (see REQUIREMENTS.md AUT-04 / ACH-A-01). That chain was eliminated rather
  // than validated: the role check is gone, replaced by a named IGRP permission checked
  // against the caller's token.
  @PreAuthorize("@igrpAuthorization.checkPermission(T(Permission).PAA_PERIODOSUBMISSAO_CRIAR)")
  @DenialMessage("Não tem permissão para criar períodos de submissão.")
  @PostMapping(value = "periods")
  @Operation(
      summary = "Create PAA Submission Period",
      description = "Cria um período de submissão PAA (UNIT ou INDIVIDUAL). "
          + "Exige a permissão paa.periodoSubmissao.criar."
  )
  public ResponseEntity<PaaSubmissionPeriodResponseDTO> createPaaSubmissionPeriod(
      @Valid @RequestBody CreatePaaSubmissionPeriodDTO createPaaSubmissionPeriodRequest) {
      final var command = new CreatePaaSubmissionPeriodCommand(createPaaSubmissionPeriodRequest);
      return commandBus.send(command);
  }

  // ACTOR-CHECK: ENFORCED -- paa.periodoSubmissao.fechar permission (IgrpAuthorizationService);
  // see note on createPaaSubmissionPeriod above -- same unverified "RH" role, eliminated the
  // same way, now checked against its own named permission.
  @PreAuthorize("@igrpAuthorization.checkPermission(T(Permission).PAA_PERIODOSUBMISSAO_FECHAR)")
  @DenialMessage("Não tem permissão para fechar períodos de submissão.")
  @PutMapping(value = "periods/{id}/close")
  @Operation(
      summary = "Close PAA Submission Period",
      description = "Fecha um período de submissão PAA. "
          + "Exige a permissão paa.periodoSubmissao.fechar."
  )
  public ResponseEntity<PaaSubmissionPeriodResponseDTO> closePaaSubmissionPeriod(
      @PathVariable(value = "id") String id) {
      final var command = new ClosePaaSubmissionPeriodCommand(java.util.UUID.fromString(id));
      return commandBus.send(command);
  }

  @GetMapping(value = "periods/active")
  @Operation(
      summary = "Get Active PAA Submission Period",
      description = "Retorna o período de submissão ativo para o tipo solicitado"
  )
  public ResponseEntity<PaaSubmissionPeriodResponseDTO> getActiveSubmissionPeriod(
      @RequestParam(value = "type") String type,
      @RequestParam(value = "purpose", required = false, defaultValue = "PAA") String purpose,
      @RequestParam(value = "year", required = false) Integer year) {
      final var query = new GetActiveSubmissionPeriodQuery(type, purpose, year);
      return queryBus.handle(query);
  }

  @GetMapping(value = "periods")
  @Operation(
      summary = "Get All PAA Submission Periods",
      description = "Lista todos os períodos de submissão PAA"
  )
  public ResponseEntity<WrapperListPaaSubmissionPeriodDTO> getAllSubmissionPeriods(
      @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
      @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSize,
      @RequestParam(value = "purpose", required = false) String purpose) {
      final var query = new GetAllSubmissionPeriodsQuery(pageNumber, pageSize, purpose);
      return queryBus.handle(query);
  }

  // ACTOR-CHECK: ENFORCED -- paa.periodoSubmissao.gerarFormularios permission (IgrpAuthorizationService).
  // Este é o primeiro sítio de chamada desta permissão, órfã desde a Fase 115 ("No call site yet
  // -- declared ahead of the period-opening form generator"). Reutiliza-se em vez de declarar
  // uma oitava permissão (D-25, 119-05-PLAN.md): quem pode mandar gerar pode ver o que foi
  // gerado, e uma permissão a mais seria uma dívida a mais para manter em passo com
  // .igrpstudio/permissions.json, correspondência que hoje nada verifica.
  @PreAuthorize("@igrpAuthorization.checkPermission(T(Permission).PAA_PERIODOSUBMISSAO_GERARFORMULARIOS)")
  @DenialMessage("Não tem permissão para consultar a geração de formulários.")
  @GetMapping(value = "periods/generation-summaries")
  @Operation(
      summary = "Get PAA Submission Period Generation Summaries",
      description = "Contagens em bloco da geração de formulários para vários períodos de submissão. "
          + "Exige a permissão paa.periodoSubmissao.gerarFormularios."
  )
  public ResponseEntity<List<FormGenerationSummaryDTO>> getPeriodGenerationSummaries(
      @RequestParam("periodIds") String periodIds) {
      final var ids = Arrays.stream(periodIds.split(","))
          .map(String::trim)
          .filter(id -> !id.isEmpty())
          .toList();
      final var query = new GetPeriodGenerationSummariesQuery(ids);
      return queryBus.handle(query);
  }

  // ACTOR-CHECK: ENFORCED -- paa.periodoSubmissao.gerarFormularios permission (IgrpAuthorizationService).
  // Segundo sítio de chamada da mesma permissão órfã desde a Fase 115 -- ver a nota acima em
  // getPeriodGenerationSummaries.
  @PreAuthorize("@igrpAuthorization.checkPermission(T(Permission).PAA_PERIODOSUBMISSAO_GERARFORMULARIOS)")
  @DenialMessage("Não tem permissão para consultar a geração de formulários.")
  @GetMapping(value = "periods/{id}/generation")
  @Operation(
      summary = "Get PAA Submission Period Generation Detail",
      description = "Leitura completa do lote de geração de formulários de um período de submissão. "
          + "Exige a permissão paa.periodoSubmissao.gerarFormularios."
  )
  public ResponseEntity<FormGenerationDetailDTO> getPeriodGeneration(
      @PathVariable(value = "id") String id) {
      final var query = new GetPeriodGenerationQuery(UUID.fromString(id));
      return queryBus.handle(query);
  }
}

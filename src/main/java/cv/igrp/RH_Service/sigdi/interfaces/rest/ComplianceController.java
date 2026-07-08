/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.sigdi.application.queries.*;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.RH_Service.sigdi.application.commands.*;
import cv.igrp.RH_Service.sigdi.application.dto.CloseEvaluationsRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CloseEvaluationsResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QUARPreviewResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QuotaValidationResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperSiadapEvaluationListDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CreateSiadapEvaluationRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ContractualizeObjectivesRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EvaluateCompetenciesRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.RecordObjectiveAchievementRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SubmitSelfEvaluationRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.FinalizeEvaluationRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.IndividualObjectiveDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CompetencyItemDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapInterimFeedbackDTO;
import java.util.List;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/compliance")
@Tag(
    name = "SIGDI-Compliance",
    description = "QUAR generation, SIADAP evaluations and quota validation"
)
public class ComplianceController {

  private final QueryBus queryBus;
  private final CommandBus commandBus;

  public ComplianceController(QueryBus queryBus, CommandBus commandBus) {
    this.queryBus = queryBus;
    this.commandBus = commandBus;
  }

  @GetMapping(value = "quar/preview")
  @Operation(
    summary = "Get QUAR preview",
    description = "Retorna os dados do QUAR em JSON para pré-visualização.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = QUARPreviewResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<QUARPreviewResponseDTO> getQUARPreview(
    @RequestParam(value = "year") Integer year,
    @RequestParam(value = "organicUnitId", required = false) String organicUnitId)
  {
    final var query = new GetQUARPreviewQuery(year, organicUnitId);
    return queryBus.handle(query);
  }

  @GetMapping(value = "quar/export")
  @Operation(
    summary = "Export QUAR",
    description = "Gera e descarrega o QUAR no formato solicitado (PDF ou EXCEL).",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "Binary file content"
      )
    }
  )
  public ResponseEntity<byte[]> exportQUAR(
    @RequestParam(value = "year") Integer year,
    @RequestParam(value = "format", required = false, defaultValue = "PDF") String format,
    @RequestParam(value = "organicUnitId", required = false) String organicUnitId,
    @RequestParam(value = "force", required = false, defaultValue = "false") Boolean force)
  {
    final var query = new GetQUARExportQuery(year, format, organicUnitId, force);
    return queryBus.handle(query);
  }

  @GetMapping(value = "siadap/evaluations")
  @Operation(
    summary = "List SIADAP evaluations",
    description = "Lista avaliações SIADAP de uma unidade orgânica.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = WrapperSiadapEvaluationListDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<WrapperSiadapEvaluationListDTO> listEvaluations(
    @RequestParam(value = "year") Integer year,
    @RequestParam(value = "organicUnitId", required = false) String organicUnitId,
    @RequestParam(value = "status", required = false) String status,
    @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
    @RequestParam(value = "pageSize",   required = false, defaultValue = "20") String pageSize)
  {
    final var query = new ListSiadapEvaluationsQuery(year, organicUnitId, status, pageNumber, pageSize);
    return queryBus.handle(query);
  }

  @GetMapping(value = "siadap/quota-validation")
  @Operation(
    summary = "Validate SIADAP quotas",
    description = "Valida o cumprimento das quotas de mérito por unidade.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = QuotaValidationResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<QuotaValidationResponseDTO> quotaValidation(
    @RequestParam(value = "year") Integer year)
  {
    final var query = new GetQuotaValidationQuery(year);
    return queryBus.handle(query);
  }

  @PostMapping(value = "siadap/evaluations/close")
  @Operation(
    summary = "Close evaluations",
    description = "Fecha as avaliações de uma unidade orgânica. Valida quotas antes de executar.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = CloseEvaluationsResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<CloseEvaluationsResponseDTO> closeEvaluations(
    @Valid @RequestBody CloseEvaluationsRequestDTO closeEvaluationsRequest)
  {
    final var command = new CloseEvaluationsCommand(closeEvaluationsRequest);
    return commandBus.send(command);
  }

  @GetMapping(value = "siadap/export")
  @Operation(
    summary = "Export SIADAP evaluations",
    description = "Exporta avaliações SIADAP em lote (EXCEL ou PDF_BATCH).",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "Binary file content"
      )
    }
  )
  public ResponseEntity<byte[]> exportSiadap(
    @RequestParam(value = "year") Integer year,
    @RequestParam(value = "organicUnitId", required = false) String organicUnitId,
    @RequestParam(value = "format", required = false, defaultValue = "EXCEL") String format)
  {
    final var query = new GetSiadapExportQuery(year, organicUnitId, format);
    return queryBus.handle(query);
  }

  @PostMapping(value = "siadap/evaluations")
  @Operation(
    summary = "Create SIADAP evaluation",
    description = "Cria uma nova avaliação no início do ciclo (fase OPEN).",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SiadapEvaluationDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<SiadapEvaluationDTO> createEvaluation(
    @Valid @RequestBody CreateSiadapEvaluationRequestDTO request)
  {
    final var command = new CreateSiadapEvaluationCommand(request);
    return commandBus.send(command);
  }

  @GetMapping(value = "siadap/evaluations/{id}")
  @Operation(
    summary = "Get SIADAP evaluation details",
    description = "Retorna os detalhes completos de uma avaliação (fases, objetivos, competências).",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SiadapEvaluationDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<SiadapEvaluationDTO> getEvaluationDetail(
    @PathVariable("id") String id)
  {
    final var query = new GetEvaluationDetailQuery(id);
    return queryBus.handle(query);
  }

  @PostMapping(value = "siadap/evaluations/{id}/objectives")
  @Operation(
    summary = "Contractualize objectives",
    description = "Define os objetivos individuais para a avaliação.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SiadapEvaluationDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<SiadapEvaluationDTO> contractualizeObjectives(
    @PathVariable("id") String id,
    @Valid @RequestBody List<IndividualObjectiveDTO> objectives)
  {
    final var request = new ContractualizeObjectivesRequestDTO(id, objectives);
    final var command = new ContractualizeObjectivesCommand(request);
    return commandBus.send(command);
  }

  @PostMapping(value = "siadap/evaluations/{id}/competencies")
  @Operation(
    summary = "Evaluate competencies",
    description = "Define e avalia as competências do colaborador.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SiadapEvaluationDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<SiadapEvaluationDTO> evaluateCompetencies(
    @PathVariable("id") String id,
    @Valid @RequestBody List<CompetencyItemDTO> competencies)
  {
    final var request = new EvaluateCompetenciesRequestDTO(id, competencies);
    final var command = new EvaluateCompetenciesCommand(request);
    return commandBus.send(command);
  }

  @PostMapping(value = "siadap/evaluations/{id}/objectives/achievements")
  @Operation(
    summary = "Record objective achievement",
    description = "Regista o valor atingido e pontuação de um objetivo específico.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SiadapEvaluationDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<SiadapEvaluationDTO> recordObjectiveAchievement(
    @PathVariable("id") String id,
    @Valid @RequestBody RecordObjectiveAchievementRequestDTO request)
  {
    request.setEvaluationId(id);
    final var command = new RecordObjectiveAchievementCommand(request);
    return commandBus.send(command);
  }

  @PostMapping(value = "siadap/evaluations/{id}/self-evaluation")
  @Operation(
    summary = "Submit self-evaluation",
    description = "Colaborador submete a sua autoavaliação.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SiadapEvaluationDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<SiadapEvaluationDTO> submitSelfEvaluation(
    @PathVariable("id") String id,
    @Valid @RequestBody SubmitSelfEvaluationRequestDTO request)
  {
    request.setEvaluationId(id);
    final var command = new SubmitSelfEvaluationCommand(request);
    return commandBus.send(command);
  }

  @PostMapping(value = "siadap/evaluations/{id}/finalize")
  @Operation(
    summary = "Finalize evaluation",
    description = "Calcula notas finais e avança a avaliação para harmonização.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SiadapEvaluationDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<SiadapEvaluationDTO> finalizeEvaluation(
    @PathVariable("id") String id)
  {
    final var request = new FinalizeEvaluationRequestDTO(id);
    final var command = new FinalizeEvaluationCommand(request);
    return commandBus.send(command);
  }

  @GetMapping(value = "siadap/evaluations/{id}/interim-feedback")
  @Operation(
    summary = "Get SIADAP interim feedback",
    description = "Retorna os dados da ficha de feedback intercalar de uma avaliação.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SiadapInterimFeedbackDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<SiadapInterimFeedbackDTO> getInterimFeedback(
    @PathVariable("id") String id)
  {
    final var query = new GetSiadapInterimFeedbackQuery(id);
    return queryBus.handle(query);
  }

  @PostMapping(value = "siadap/evaluations/{id}/interim-feedback")
  @Operation(
    summary = "Save SIADAP interim feedback",
    description = "Salva os dados da ficha de feedback intercalar de uma avaliação.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SiadapInterimFeedbackDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<SiadapInterimFeedbackDTO> saveInterimFeedback(
    @PathVariable("id") String id,
    @Valid @RequestBody SiadapInterimFeedbackDTO body)
  {
    final var command = new SaveSiadapInterimFeedbackCommand(id, body);
    return commandBus.send(command);
  }
}

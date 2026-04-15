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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.sigdi.application.queries.*;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.RH_Service.sigdi.application.commands.*;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperListTaticalActivityDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CreateTacticalActivityDTO;
import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.TaticalActivityStatusDTO;
import java.util.Map;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultCheckinRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperKeyResultListDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ActivityWorkflowResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WorkflowCommentDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ChangeRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ChangeRequestResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CreateOkrDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OkrResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityDetailDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperWorkflowInboxDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/tactical")
@Tag(
    name = "Sigdi",
    description = "gest strategies"
)
public class TaticalController {

  
  private final QueryBus queryBus;
  private final CommandBus commandBus;

  public TaticalController(QueryBus queryBus, CommandBus commandBus) {
          this.queryBus = queryBus;
          this.commandBus = commandBus;
  }
   @GetMapping(
   value = "activities"
  )
  @Operation(
    summary = "Get tatical activities",
    description = "Get tatical activities",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = WrapperListTaticalActivityDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<WrapperListTaticalActivityDTO> getTaticalActivities(
    @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
    @RequestParam(value = "pageSize", required = false) String pageSize,
    @RequestParam(value = "status", required = false) String status,
    @RequestParam(value = "unidade", required = false) String unidade,
    @RequestParam(value = "data", required = false) String data)
  {

      final var query = new GetTaticalActivitiesQuery(pageNumber, pageSize, status, unidade, data);

      return queryBus.handle(query);

  }

   @PostMapping(
   value = "activities"
  )
  @Operation(
    summary = "Create tactical activity",
    description = "Create tactical activity",
    responses = {
      @ApiResponse(
          responseCode = "201",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = TacticalActivityResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<TacticalActivityResponseDTO> createTacticalActivity(@Valid @RequestBody CreateTacticalActivityDTO createTacticalActivityRequest
    )
  {

      final var command = new CreateTacticalActivityCommand(createTacticalActivityRequest);

      return commandBus.send(command);

  }

   @PatchMapping(
   value = "activities/{id}/status"
  )
  @Operation(
    summary = "Change status tactical activity",
    description = "Change status tactical activity",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = String.class,
                  type = "String")
          )
      )
    }
  )
  
  public ResponseEntity<Map<String, ?>> changeStatusTacticalActivity(@Valid @RequestBody TaticalActivityStatusDTO changeStatusTacticalActivityRequest
    , @PathVariable(value = "id") String id)
  {

      final var command = new ChangeStatusTacticalActivityCommand(changeStatusTacticalActivityRequest, id);

      return commandBus.send(command);

  }

   @PostMapping(
   value = "krs/{id}/checkin"
  )
  @Operation(
    summary = "Registra processo kr",
    description = "Registra processo kr",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = String.class,
                  type = "String")
          )
      )
    }
  )
  
  public ResponseEntity<String> registraProcessoKr(@Valid @RequestBody KeyResultCheckinRequestDTO registraProcessoKrRequest
    , @PathVariable(value = "id") String id)
  {

      final var command = new RegistraProcessoKrCommand(registraProcessoKrRequest, id);

      return commandBus.send(command);

  }

   @PostMapping(
   value = "krs"
  )
  @Operation(
    summary = "Create key result",
    description = "Create key result",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = KeyResultResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<KeyResultResponseDTO> createKeyResult(@Valid @RequestBody KeyResultRequestDTO createKeyResultRequest
    )
  {

      final var command = new CreateKeyResultCommand(createKeyResultRequest);

      return commandBus.send(command);

  }

   @GetMapping(
   value = "krs/{id}"
  )
  @Operation(
    summary = "Get key result",
    description = "Get key result",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = KeyResultResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<KeyResultResponseDTO> getKeyResult(
    @PathVariable(value = "id") String id)
  {

      final var query = new GetKeyResultQuery(id);

      return queryBus.handle(query);

  }

   @GetMapping(
   value = "krs"
  )
  @Operation(
    summary = "Get all key results",
    description = "Get all key results",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = WrapperKeyResultListDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<WrapperKeyResultListDTO> getAllKeyResults(
    @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
    @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSize)
  {

      final var query = new GetAllKeyResultsQuery(pageNumber, pageSize);

      return queryBus.handle(query);

  }

   @PutMapping(
   value = "krs/{id}"
  )
  @Operation(
    summary = "Update key result",
    description = "Update key result",
    responses = {
      @ApiResponse(
          responseCode = "200",

          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = KeyResultResponseDTO.class,
                  type = "object")
          )
      )
    }
  )

  public ResponseEntity<KeyResultResponseDTO> updateKeyResult(@Valid @RequestBody KeyResultRequestDTO updateKeyResultRequest
    , @PathVariable(value = "id") String id)
  {

      final var command = new UpdateKeyResultCommand(updateKeyResultRequest, id);

      return commandBus.send(command);

  }

   @PostMapping(
   value = "okrs"
  )
  @Operation(
    summary = "Create okr",
    description = "Create okr",
    responses = {
      @ApiResponse(
          responseCode = "201",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = OkrResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  public ResponseEntity<OkrResponseDTO> createOkr(@Valid @RequestBody CreateOkrDTO createOkrRequest
    )
  {
      final var command = new CreateOkrCommand(createOkrRequest);
      return commandBus.send(command);
  }

   @GetMapping(
   value = "activities/{id}"
  )
  @Operation(
    summary = "Get tactical activity by id",
    description = "Get tactical activity by id",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = TacticalActivityDetailDTO.class,
                  type = "object")
          )
      )
    }
  )
  public ResponseEntity<TacticalActivityDetailDTO> getTacticalActivityById(
    @PathVariable(value = "id") String id)
  {
      final var query = new GetTacticalActivityByIdQuery(id);
      return queryBus.handle(query);
  }

   @PostMapping(
   value = "activities/{id}/submit"
  )
  @Operation(
    summary = "Submit tactical activity",
    description = "Submete uma atividade DRAFT para aprovação",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ActivityWorkflowResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<ActivityWorkflowResponseDTO> submitTacticalActivity(
    @PathVariable(value = "id") String id)
  {
      final var command = new SubmitTacticalActivityCommand(id);
      return commandBus.send(command);
  }

   @PostMapping(
   value = "activities/{id}/approve"
  )
  @Operation(
    summary = "Approve tactical activity",
    description = "Aprova uma atividade no nível de workflow do utilizador autenticado",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ActivityWorkflowResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<ActivityWorkflowResponseDTO> approveTacticalActivity(
    @RequestBody(required = false) WorkflowCommentDTO approveRequest,
    @PathVariable(value = "id") String id)
  {
      final var command = new ApproveTacticalActivityCommand(approveRequest, id);
      return commandBus.send(command);
  }

   @PostMapping(
   value = "activities/{id}/reject"
  )
  @Operation(
    summary = "Reject tactical activity",
    description = "Rejeita uma atividade com comentário obrigatório",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ActivityWorkflowResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<ActivityWorkflowResponseDTO> rejectTacticalActivity(
    @Valid @RequestBody WorkflowCommentDTO rejectRequest,
    @PathVariable(value = "id") String id)
  {
      final var command = new RejectTacticalActivityCommand(rejectRequest, id);
      return commandBus.send(command);
  }

   @GetMapping(
   value = "workflow/inbox"
  )
  @Operation(
    summary = "Get workflow inbox",
    description = "Retorna as pendências de aprovação do utilizador autenticado",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = WrapperWorkflowInboxDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<WrapperWorkflowInboxDTO> getWorkflowInbox(
    @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
    @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSize)
  {
      final var query = new GetWorkflowInboxQuery(pageNumber, pageSize);
      return queryBus.handle(query);
  }

   @PostMapping(
   value = "activities/{activityId}/change-requests"
  )
  @Operation(
    summary = "Create change request",
    description = "Solicita alteração de um campo numa atividade APPROVED",
    responses = {
      @ApiResponse(
          responseCode = "201",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ChangeRequestResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<ChangeRequestResponseDTO> createChangeRequest(
    @Valid @RequestBody ChangeRequestDTO changeRequestBody,
    @PathVariable(value = "activityId") String activityId)
  {
      final var command = new CreateChangeRequestCommand(changeRequestBody, activityId);
      return commandBus.send(command);
  }

   @PostMapping(
   value = "change-requests/{id}/approve"
  )
  @Operation(
    summary = "Approve change request",
    description = "Aprova um Change Request aplicando a alteração à atividade",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ChangeRequestResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<ChangeRequestResponseDTO> approveChangeRequest(
    @RequestBody(required = false) WorkflowCommentDTO approveRequest,
    @PathVariable(value = "id") String id)
  {
      final var command = new ApproveChangeRequestCommand(approveRequest, id);
      return commandBus.send(command);
  }

   @PostMapping(
   value = "change-requests/{id}/reject"
  )
  @Operation(
    summary = "Reject change request",
    description = "Rejeita um Change Request",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ChangeRequestResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<ChangeRequestResponseDTO> rejectChangeRequest(
    @Valid @RequestBody WorkflowCommentDTO rejectRequest,
    @PathVariable(value = "id") String id)
  {
      final var command = new RejectChangeRequestCommand(rejectRequest, id);
      return commandBus.send(command);
  }

}
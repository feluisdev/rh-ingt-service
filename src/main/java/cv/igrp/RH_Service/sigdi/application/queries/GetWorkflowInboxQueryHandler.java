package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.ChangeRequestField;
import cv.igrp.RH_Service.sigdi.application.constants.ChangeRequestStatus;
import cv.igrp.RH_Service.sigdi.application.constants.TacticalActivityStatus;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperWorkflowInboxDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WorkflowInboxItemDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.ChangeRequestRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PendingActivityRow;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PendingChangeRequestRow;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Compõe duas fontes de pendências -- atividades táticas e pedidos de alteração -- num só
 * envelope paginado (D-P, {@code 110-02-PLAN.md}). Antes deste plano só existia a primeira
 * fonte: não havia leitura nenhuma de pedidos de alteração em todo o backend.
 */
@Component
public class GetWorkflowInboxQueryHandler
    implements QueryHandler<GetWorkflowInboxQuery, ResponseEntity<WrapperWorkflowInboxDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetWorkflowInboxQueryHandler.class);

  // Vocabulário fechado de WorkflowItem.type no frontend (src/app/(myapp)/types/workflow.ts).
  // Mudar uma destas literais sem mudar do outro lado faz o ecrã cair no ramo UNDEFINED e os
  // botões de aprovação deixam de agir.
  private static final String TYPE_PAA = "PAA";
  private static final String TYPE_CHANGE_REQUEST = "CHANGE_REQUEST";

  // Cabo Verde é UTC-1 o ano inteiro; nunca assumir o fuso do servidor (regra 7, CLAUDE.md).
  // Reutiliza a constante existente em vez de a redeclarar (grep confirmou AppTimeZone.CABO_VERDE).
  private static final ZoneId CABO_VERDE = AppTimeZone.CABO_VERDE;

  // Limite superior de página (D-U): sem ele, pageSize=1000000 seria obedecido e, com duas
  // fontes, duplicado no custo de leitura.
  private static final int MAX_PAGE_SIZE = 100;

  private final TacticalActivityRepository activityRepository;
  private final ChangeRequestRepository changeRequestRepository;

  public GetWorkflowInboxQueryHandler(
      TacticalActivityRepository activityRepository,
      ChangeRequestRepository changeRequestRepository) {
    this.activityRepository = activityRepository;
    this.changeRequestRepository = changeRequestRepository;
  }

  @IgrpQueryHandler
  public ResponseEntity<WrapperWorkflowInboxDTO> handle(GetWorkflowInboxQuery query) {
    LOGGER.debug("GetWorkflowInboxQuery: {}", query);

    int page = Math.max(0, parseOrDefault(query.getPageNumber(), 0));
    int size = Math.min(MAX_PAGE_SIZE, Math.max(1, parseOrDefault(query.getPageSize(), 20)));

    List<String> pendingStatuses = List.of(
        TacticalActivityStatus.PENDING_TACTICAL.getCode(),
        TacticalActivityStatus.PENDING_STRATEGIC.getCode()
    );

    // Resolvido uma só vez: todos os itens da mesma resposta são calculados contra o mesmo dia.
    LocalDate today = LocalDate.now(CABO_VERDE);

    List<PendingActivityRow> activityRows = activityRepository.findPendingRows(pendingStatuses, page, size);
    List<PendingChangeRequestRow> changeRequestRows = changeRequestRepository.findPendingRows(page, size);
    long totalActivities = activityRepository.countByStatuses(pendingStatuses);
    long totalChangeRequests = changeRequestRepository.countPending();

    List<WorkflowInboxItemDTO> data = new ArrayList<>();
    activityRows.stream().map(row -> toInboxItem(row, today)).forEach(data::add);
    changeRequestRows.stream().map(row -> toInboxItem(row, today)).forEach(data::add);

    WrapperWorkflowInboxDTO response = new WrapperWorkflowInboxDTO();
    response.setData(data);
    response.setPageNumber(page);
    response.setPageSize(size);
    response.setTotalElements(totalActivities + totalChangeRequests);
    // NÃO é ceil(totalElements/size): cada fonte pagina de forma independente com o mesmo
    // page/size (D-P), pelo que o número de páginas é o máximo entre as duas, não o total
    // combinado dividido pelo tamanho. Exemplo: 25 atividades + 25 pedidos, size=20 -- cada
    // fonte esgota-se em 2 páginas (página 0 traz 20+20=40, página 1 traz 5+5=10); ceil(50/20)
    // daria 3 e anunciaria uma terceira página que devolveria zero itens.
    response.setTotalPages(Math.max(pagesOf(totalActivities, size), pagesOf(totalChangeRequests, size)));

    return ResponseEntity.ok(response);
  }

  private int pagesOf(long total, int size) {
    return (int) Math.ceil((double) total / size);
  }

  private WorkflowInboxItemDTO toInboxItem(PendingActivityRow row, LocalDate today) {
    WorkflowInboxItemDTO dto = new WorkflowInboxItemDTO();
    dto.setId(row.id());
    dto.setTitle(row.title());
    dto.setCurrentStatus(row.status());
    dto.setBudgetEstimated(row.budgetEstimated());
    dto.setEconomicClassifier(row.economicClassifier());
    dto.setType(TYPE_PAA);
    dto.setRequestedBy(row.requestedBy());
    applyRequestedTiming(dto, row.requestedAt(), today);
    return dto;
  }

  private WorkflowInboxItemDTO toInboxItem(PendingChangeRequestRow row, LocalDate today) {
    WorkflowInboxItemDTO dto = new WorkflowInboxItemDTO();
    dto.setId(row.id());
    dto.setTitle(buildChangeRequestTitle(row));
    dto.setCurrentStatus(ChangeRequestStatus.PENDING.getCode());
    // Derivar de proposedValue seria apresentar como orçamento uma string que ainda não é
    // orçamento nenhum -- o backend não formata montante nenhum aqui (D-B).
    dto.setBudgetEstimated(null);
    dto.setEconomicClassifier(null);
    dto.setType(TYPE_CHANGE_REQUEST);
    dto.setRequestedBy(row.requestedBy());
    applyRequestedTiming(dto, row.requestedAt(), today);
    return dto;
  }

  private String buildChangeRequestTitle(PendingChangeRequestRow row) {
    // fromCode, não fromCodeOrThrow (D-T): uma linha corrompida não pode esvaziar a caixa
    // inteira -- se o código for desconhecido, o título mostra o código cru em vez de lançar.
    String etiqueta = ChangeRequestField.fromCode(row.fieldName())
        .map(ChangeRequestField::getDescription)
        .orElse(row.fieldName());
    return "Alteração de " + etiqueta + ": " + valueOrEmpty(row.currentValue())
        + " → " + valueOrEmpty(row.proposedValue()) + " — " + row.activityTitle();
  }

  private String valueOrEmpty(String value) {
    return (value == null || value.isBlank()) ? "(vazio)" : value;
  }

  private void applyRequestedTiming(WorkflowInboxItemDTO dto, LocalDateTime requestedAt, LocalDate today) {
    if (requestedAt == null) {
      // requestedAt é NOT NULL na base, mas uma linha anómala não pode derrubar o handler;
      // zero aqui significa "sem data", não "pedido hoje".
      dto.setRequestedDate(null);
      dto.setPendingSinceDays(0);
      return;
    }
    dto.setRequestedDate(requestedAt.toLocalDate());
    dto.setPendingSinceDays((int) Math.max(0, ChronoUnit.DAYS.between(requestedAt.toLocalDate(), today)));
  }

  private int parseOrDefault(String value, int defaultValue) {
    try {
      return (value != null && !value.isBlank()) ? Integer.parseInt(value) : defaultValue;
    } catch (NumberFormatException e) {
      throw IgrpResponseStatusException.badRequest("Parâmetro de paginação inválido: " + value);
    }
  }
}

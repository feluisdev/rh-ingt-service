package cv.igrp.RH_Service.sigdi.application.queries;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.sigdi.application.dto.WorkflowInboxItemDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperWorkflowInboxDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.ChangeRequestRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PendingActivityRow;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PendingChangeRequestRow;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * Primeiro teste de {@link GetWorkflowInboxQueryHandler}. Antes deste plano o handler não tinha
 * nenhum teste próprio -- {@code grep -rl "GetWorkflowInboxQueryHandler" src/test} não devolvia
 * "vazio" como a leitura ingénua do 110-RESEARCH.md sugeria: já devolvia um ficheiro,
 * {@code GetDashboardSummaryQueryHandlerTest.java}, mas esse teste mocka
 * {@code GetWorkflowInboxQueryHandler} como colaborador de outro handler e nunca exerce a lógica
 * de composição dele. Este é o primeiro teste que chama {@code handler.handle(...)} desta classe.
 */
@ExtendWith(MockitoExtension.class)
class GetWorkflowInboxQueryHandlerTest {

  @Mock
  private TacticalActivityRepository activityRepository;

  @Mock
  private ChangeRequestRepository changeRequestRepository;

  @InjectMocks
  private GetWorkflowInboxQueryHandler handler;

  private static PendingActivityRow activityRow(LocalDateTime requestedAt) {
    return new PendingActivityRow(UUID.randomUUID(), "Reforçar a rede de saneamento",
        "PENDING_TACTICAL", new BigDecimal("40000"), "02.02.01", "6a1f...ativ", requestedAt);
  }

  private static PendingChangeRequestRow changeRequestRow(String fieldName, LocalDateTime requestedAt) {
    return new PendingChangeRequestRow(UUID.randomUUID(), UUID.randomUUID(), "Atividade base",
        fieldName, "1000", "2500", "6a1f...pedi", requestedAt);
  }

  private GetWorkflowInboxQuery query(String pageNumber, String pageSize) {
    return new GetWorkflowInboxQuery(pageNumber, pageSize);
  }

  // Caso 1 -- Composição: uma atividade e um pedido devolvem dois itens, com "PAA" e
  // "CHANGE_REQUEST" comparados com as literais escritas à mão aqui, não com as constantes
  // privadas do handler -- uma constante partilhada não deteta a sua própria mudança.
  @Test
  void handle_composesOneActivityAndOnePendingChangeRequest() {
    when(activityRepository.findPendingRows(anyList(), eq(0), eq(20)))
        .thenReturn(List.of(activityRow(LocalDateTime.now(AppTimeZone.CABO_VERDE).minusDays(1))));
    when(changeRequestRepository.findPendingRows(eq(0), eq(20)))
        .thenReturn(List.of(changeRequestRow("budget", LocalDateTime.now(AppTimeZone.CABO_VERDE).minusDays(2))));
    when(activityRepository.countByStatuses(anyList())).thenReturn(1L);
    when(changeRequestRepository.countPending()).thenReturn(1L);

    ResponseEntity<WrapperWorkflowInboxDTO> response = handler.handle(query(null, null));

    List<WorkflowInboxItemDTO> data = response.getBody().getData();
    assertEquals(2, data.size());
    assertEquals("PAA", data.get(0).getType());
    assertEquals("CHANGE_REQUEST", data.get(1).getType());
  }

  // Caso 2 -- Total verdadeiro: contagens 7 e 5 dão totalElements 12, mesmo que a página
  // (mockada aqui com só 2 linhas) traga menos itens do que isso.
  @Test
  void handle_totalElementsIsTheTrueCountNotTheSizeOfThePageData() {
    when(activityRepository.findPendingRows(anyList(), eq(0), eq(20)))
        .thenReturn(List.of(activityRow(LocalDateTime.now(AppTimeZone.CABO_VERDE))));
    when(changeRequestRepository.findPendingRows(eq(0), eq(20)))
        .thenReturn(List.of(changeRequestRow("title", LocalDateTime.now(AppTimeZone.CABO_VERDE))));
    when(activityRepository.countByStatuses(anyList())).thenReturn(7L);
    when(changeRequestRepository.countPending()).thenReturn(5L);

    ResponseEntity<WrapperWorkflowInboxDTO> response = handler.handle(query(null, null));

    assertEquals(12L, response.getBody().getTotalElements());
    assertTrue(response.getBody().getData().size() < 12,
        "totalElements não pode ser confundido com data.size() -- a página trouxe menos itens");
  }

  // Caso 3 -- Páginas pela fonte mais longa: 25 e 25 com size=20 dão totalPages 2, não 3.
  // ceil(50/20) = 3 anunciaria uma terceira página que devolveria zero itens.
  @Test
  void handle_totalPagesIsTheMaxPerSourceNotCeilOfTheCombinedTotal() {
    when(activityRepository.findPendingRows(anyList(), eq(0), eq(20))).thenReturn(Collections.emptyList());
    when(changeRequestRepository.findPendingRows(eq(0), eq(20))).thenReturn(Collections.emptyList());
    when(activityRepository.countByStatuses(anyList())).thenReturn(25L);
    when(changeRequestRepository.countPending()).thenReturn(25L);

    ResponseEntity<WrapperWorkflowInboxDTO> response = handler.handle(query("0", "20"));

    assertEquals(2, response.getBody().getTotalPages(),
        "25 atividades + 25 pedidos, size=20: cada fonte esgota-se em 2 páginas -- "
            + "ceil(50/20)=3 estaria errado, anunciaria uma página vazia");
  }

  // Caso 4 -- Contagens diferentes: a resposta executável à pergunta "o que acontece quando as
  // duas fontes têm contagens diferentes". Fonte A (atividades) com 1 linha, fonte B (pedidos)
  // com 25, size=20: page=0 traz 21 itens (1+20); page=1 traz só os 5 restantes de B, todos
  // CHANGE_REQUEST, porque A já se esgotou na página 0.
  @Test
  void handle_unevenSourceCounts_shorterSourceExhaustsFirstWithoutLosingOrDuplicatingItems() {
    when(activityRepository.findPendingRows(anyList(), eq(0), eq(20)))
        .thenReturn(List.of(activityRow(LocalDateTime.now(AppTimeZone.CABO_VERDE))));
    when(changeRequestRepository.findPendingRows(eq(0), eq(20)))
        .thenReturn(fixedSizeChangeRequests(20));
    when(activityRepository.countByStatuses(anyList())).thenReturn(1L);
    when(changeRequestRepository.countPending()).thenReturn(25L);

    ResponseEntity<WrapperWorkflowInboxDTO> page0 = handler.handle(query("0", "20"));
    assertEquals(21, page0.getBody().getData().size());

    when(activityRepository.findPendingRows(anyList(), eq(1), eq(20)))
        .thenReturn(Collections.emptyList());
    when(changeRequestRepository.findPendingRows(eq(1), eq(20)))
        .thenReturn(fixedSizeChangeRequests(5));

    ResponseEntity<WrapperWorkflowInboxDTO> page1 = handler.handle(query("1", "20"));
    List<WorkflowInboxItemDTO> page1Data = page1.getBody().getData();
    assertEquals(5, page1Data.size());
    assertTrue(page1Data.stream().allMatch(item -> "CHANGE_REQUEST".equals(item.getType())));
  }

  private static List<PendingChangeRequestRow> fixedSizeChangeRequests(int count) {
    List<PendingChangeRequestRow> rows = new java.util.ArrayList<>();
    for (int i = 0; i < count; i++) {
      rows.add(changeRequestRow("title", LocalDateTime.now(AppTimeZone.CABO_VERDE)));
    }
    return rows;
  }

  // Caso 5 -- Dias em espera: requestedAt de há 5 dias dá pendingSinceDays 5 e requestedDate
  // igual a requestedAt.toLocalDate(); um requestedAt no futuro dá 0, nunca um negativo.
  @Test
  void handle_pendingSinceDays_derivesFromRequestedAtAndNeverGoesNegative() {
    LocalDateTime fiveDaysAgo = LocalDateTime.now(AppTimeZone.CABO_VERDE).minusDays(5);
    LocalDateTime inTheFuture = LocalDateTime.now(AppTimeZone.CABO_VERDE).plusDays(2);

    when(activityRepository.findPendingRows(anyList(), eq(0), eq(20)))
        .thenReturn(List.of(activityRow(fiveDaysAgo)));
    when(changeRequestRepository.findPendingRows(eq(0), eq(20)))
        .thenReturn(List.of(changeRequestRow("title", inTheFuture)));
    when(activityRepository.countByStatuses(anyList())).thenReturn(1L);
    when(changeRequestRepository.countPending()).thenReturn(1L);

    ResponseEntity<WrapperWorkflowInboxDTO> response = handler.handle(query(null, null));

    WorkflowInboxItemDTO activityItem = response.getBody().getData().get(0);
    assertEquals(5, activityItem.getPendingSinceDays());
    assertEquals(fiveDaysAgo.toLocalDate(), activityItem.getRequestedDate());

    WorkflowInboxItemDTO changeRequestItem = response.getBody().getData().get(1);
    assertEquals(0, changeRequestItem.getPendingSinceDays(),
        "requestedAt no futuro tem de dar 0, nunca um valor negativo");
  }

  // Caso 6 -- Um field_name fora da lista fechada não pode esvaziar a caixa (D-T): fromCode em
  // vez de fromCodeOrThrow. A resposta continua 200, o título mostra o código cru, e o outro
  // item da resposta continua presente.
  @Test
  void handle_unknownChangeRequestFieldCode_rendersRawCodeInsteadOfThrowing() {
    when(activityRepository.findPendingRows(anyList(), eq(0), eq(20)))
        .thenReturn(List.of(activityRow(LocalDateTime.now(AppTimeZone.CABO_VERDE))));
    when(changeRequestRepository.findPendingRows(eq(0), eq(20)))
        .thenReturn(List.of(changeRequestRow("orcamento_XPTO", LocalDateTime.now(AppTimeZone.CABO_VERDE))));
    when(activityRepository.countByStatuses(anyList())).thenReturn(1L);
    when(changeRequestRepository.countPending()).thenReturn(1L);

    ResponseEntity<WrapperWorkflowInboxDTO> response = assertDoesNotThrow(() -> handler.handle(query(null, null)));

    assertEquals(200, response.getStatusCode().value());
    List<WorkflowInboxItemDTO> data = response.getBody().getData();
    assertEquals(2, data.size(), "o outro item da resposta continua presente");
    WorkflowInboxItemDTO changeRequestItem = data.get(1);
    assertTrue(changeRequestItem.getTitle().contains("orcamento_XPTO"),
        "código desconhecido aparece cru no título em vez de lançar exceção");
  }

  // Caso 7 -- Limites de paginação (D-U, T-110-04/T-110-05): pageSize=1000000 é obedecido só até
  // 100, nas duas fontes; pageNumber=-3 não é obedecido, fica 0; pageSize=0 nunca chega a
  // PageRequest.of, fica 1.
  @Test
  void handle_pageParametersAreClampedInBothDirectionsOnBothSources() {
    ArgumentCaptor<Integer> activitySize = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> requestSize = ArgumentCaptor.forClass(Integer.class);
    when(activityRepository.findPendingRows(anyList(), anyInt(), activitySize.capture()))
        .thenReturn(Collections.emptyList());
    when(changeRequestRepository.findPendingRows(anyInt(), requestSize.capture()))
        .thenReturn(Collections.emptyList());
    when(activityRepository.countByStatuses(anyList())).thenReturn(0L);
    when(changeRequestRepository.countPending()).thenReturn(0L);

    handler.handle(query(null, "1000000"));

    assertEquals(100, activitySize.getValue());
    assertEquals(100, requestSize.getValue());

    reset(activityRepository, changeRequestRepository);

    ArgumentCaptor<Integer> activityPage = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> requestPage = ArgumentCaptor.forClass(Integer.class);
    when(activityRepository.findPendingRows(anyList(), activityPage.capture(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(changeRequestRepository.findPendingRows(requestPage.capture(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(activityRepository.countByStatuses(anyList())).thenReturn(0L);
    when(changeRequestRepository.countPending()).thenReturn(0L);

    handler.handle(query("-3", null));

    assertEquals(0, activityPage.getValue());
    assertEquals(0, requestPage.getValue());

    reset(activityRepository, changeRequestRepository);

    ArgumentCaptor<Integer> activityZeroSize = ArgumentCaptor.forClass(Integer.class);
    when(activityRepository.findPendingRows(anyList(), anyInt(), activityZeroSize.capture()))
        .thenReturn(Collections.emptyList());
    when(changeRequestRepository.findPendingRows(anyInt(), anyInt())).thenReturn(Collections.emptyList());
    when(activityRepository.countByStatuses(anyList())).thenReturn(0L);
    when(changeRequestRepository.countPending()).thenReturn(0L);

    handler.handle(query(null, "0"));

    assertEquals(1, activityZeroSize.getValue(), "pageSize=0 nunca pode chegar a PageRequest.of");
  }

  // Caso 8 -- Duas fontes vazias: 200, lista vazia, totalElements 0, totalPages 0, sem exceção.
  @Test
  void handle_bothSourcesEmpty_returnsEmptyEnvelopeWithoutThrowing() {
    when(activityRepository.findPendingRows(anyList(), eq(0), eq(20))).thenReturn(Collections.emptyList());
    when(changeRequestRepository.findPendingRows(eq(0), eq(20))).thenReturn(Collections.emptyList());
    when(activityRepository.countByStatuses(anyList())).thenReturn(0L);
    when(changeRequestRepository.countPending()).thenReturn(0L);

    ResponseEntity<WrapperWorkflowInboxDTO> response = assertDoesNotThrow(() -> handler.handle(query(null, null)));

    assertEquals(200, response.getStatusCode().value());
    assertTrue(response.getBody().getData().isEmpty());
    assertEquals(0L, response.getBody().getTotalElements());
    assertEquals(0, response.getBody().getTotalPages());
  }
}

package cv.igrp.RH_Service.sigdi.application.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperSiadapEvaluationListDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapEvaluationEntityRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * SIA-01, critérios 1, 2, 3 e 5: prova de que {@link ListSiadapEvaluationsQueryHandler} compõe
 * o filtro pelos três eixos (ano, unidade orgânica, fase) via
 * {@code SiadapEvaluationSpecifications.byFilters} e falha fechado num código de fase inválido --
 * antes de qualquer consulta ao repositório. Todas as asserções sobre o filtro são feitas sobre a
 * {@link Specification} capturada por {@link ArgumentCaptor}, invocada contra {@link Root}/
 * {@link CriteriaBuilder} mockados; nenhuma é sobre o número de linhas devolvidas (molde de
 * {@code SiadapEvaluationRepositoryImplTest}, Plano 100-01).
 * <p>
 * D-04: o handler continua a montar o DTO a partir da {@link SiadapEvaluationEntity} pelo
 * {@code toDto} privado, byte a byte igual ao que já era -- não delega em
 * {@code SiadapEvaluationMapper.toFullDto}.
 */
@ExtendWith(MockitoExtension.class)
class ListSiadapEvaluationsQueryHandlerTest {

  @Mock
  private SiadapEvaluationEntityRepository repository;

  @Mock
  private FuncionarioLookupPort funcionarioLookupPort;

  @Mock
  private OrganicaLookupPort organicaLookupPort;

  @InjectMocks
  private ListSiadapEvaluationsQueryHandler handler;

  @Mock
  private Root<SiadapEvaluationEntity> root;

  @Mock
  private CriteriaQuery<?> query;

  @Mock
  private CriteriaBuilder cb;

  @SuppressWarnings("unchecked")
  private void stubCriteriaMocks() {
    Path<Object> yearPath = mock(Path.class);
    Path<Object> organicUnitIdPath = mock(Path.class);
    Path<Object> evaluationPhasePath = mock(Path.class);

    Mockito.lenient().doReturn(yearPath).when(root).get("year");
    Mockito.lenient().doReturn(organicUnitIdPath).when(root).get("organicUnitId");
    Mockito.lenient().doReturn(evaluationPhasePath).when(root).get("evaluationPhase");

    Mockito.lenient().when(cb.equal(any(), any(Object.class))).thenReturn(mock(Predicate.class));
    Mockito.lenient().when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));
  }

  @SuppressWarnings("unchecked")
  private Specification<SiadapEvaluationEntity> captureSpecification() {
    ArgumentCaptor<Specification<SiadapEvaluationEntity>> specCaptor =
        ArgumentCaptor.forClass(Specification.class);
    verify(repository).findAll(specCaptor.capture(), any(Pageable.class));
    return specCaptor.getValue();
  }

  private void stubEmptyPage() {
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));
  }

  // Critério 3 -- interseção dos três eixos.
  @Test
  void handleWithYearOrganicUnitAndStatusProducesThreePredicates() {
    stubCriteriaMocks();
    stubEmptyPage();

    ListSiadapEvaluationsQuery q = new ListSiadapEvaluationsQuery(2026, "unit-1", "HARMONIZATION", "0", "20");

    handler.handle(q);

    Specification<SiadapEvaluationEntity> spec = captureSpecification();
    spec.toPredicate(root, query, cb);

    verify(cb, times(3)).equal(any(), any(Object.class));
    verify(repository, never()).findByYear(anyString(), any(Pageable.class));
  }

  // Critério 1 -- fase isolada, sem unidade orgânica.
  @Test
  void handleWithYearAndStatusOnlyProducesTwoPredicatesAndNeverTouchesOrganicUnit() {
    stubCriteriaMocks();
    stubEmptyPage();

    ListSiadapEvaluationsQuery q = new ListSiadapEvaluationsQuery(2026, null, "HARMONIZATION", "0", "20");

    handler.handle(q);

    Specification<SiadapEvaluationEntity> spec = captureSpecification();
    spec.toPredicate(root, query, cb);

    verify(cb, times(2)).equal(any(), any(Object.class));
    verify(root, never()).get("organicUnitId");
    verify(repository, never()).findByYear(anyString(), any(Pageable.class));
  }

  // Critério 2 -- unidade isolada, sem fase; resultado vazio vem da BD, não de um ramo especial.
  @Test
  void handleWithYearAndOrganicUnitOnlyProducesTwoPredicatesAndNeverTouchesPhase() {
    stubCriteriaMocks();
    stubEmptyPage();

    ListSiadapEvaluationsQuery q = new ListSiadapEvaluationsQuery(2026, "unit-sem-avaliacoes", null, "0", "20");

    handler.handle(q);

    Specification<SiadapEvaluationEntity> spec = captureSpecification();
    spec.toPredicate(root, query, cb);

    verify(cb, times(2)).equal(any(), any(Object.class));
    verify(root, never()).get("evaluationPhase");
    verify(repository, never()).findByYear(anyString(), any(Pageable.class));
  }

  // Critério 5 -- falha visível, antes de qualquer consulta.
  @Test
  void handleWithInvalidStatusThrowsBadRequestBeforeQueryingRepository() {
    ListSiadapEvaluationsQuery q = new ListSiadapEvaluationsQuery(2026, null, "NAO_EXISTE", "0", "20");

    IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
        () -> handler.handle(q));

    assertEquals(400, exception.getBody().getStatus());
    verify(repository, never()).findAll(any(Specification.class), any(Pageable.class));
    verify(repository, never()).findByYear(anyString(), any(Pageable.class));
  }

  // Status vazio/em branco é ausência de filtro, não erro.
  @Test
  void handleWithBlankStatusIsTreatedAsNoPhaseFilterNotAsError() {
    stubCriteriaMocks();
    stubEmptyPage();

    ListSiadapEvaluationsQuery q = new ListSiadapEvaluationsQuery(2026, null, "   ", "0", "20");

    handler.handle(q);

    Specification<SiadapEvaluationEntity> spec = captureSpecification();
    spec.toPredicate(root, query, cb);

    verify(root, never()).get("evaluationPhase");
    verify(repository, never()).findByYear(anyString(), any(Pageable.class));
  }

  // D-04 -- a forma da resposta não muda: employeeName, organicUnitName e lastUpdatedAt continuam presentes.
  @Test
  void handleStillPopulatesEmployeeNameOrganicUnitNameAndLastUpdatedAtFromCurrentToDto() {
    UUID employeeId = UUID.randomUUID();
    UUID organicUnitId = UUID.randomUUID();

    SiadapEvaluationEntity entity = new SiadapEvaluationEntity();
    entity.setId(UUID.randomUUID());
    entity.setEmployeeId(employeeId.toString());
    entity.setYear("2026");
    entity.setOrganicUnitId(organicUnitId.toString());
    entity.setEvaluationPhase("HARMONIZATION");
    entity.setValidatedQuota(false);
    ReflectionTestUtils.setField(entity, "lastModifiedDate", LocalDateTime.of(2026, 8, 21, 10, 0));

    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));
    when(funcionarioLookupPort.findById(employeeId))
        .thenReturn(Optional.of(new FuncionarioDTO(employeeId.toString(), "Maria Silva")));
    when(organicaLookupPort.findById(organicUnitId))
        .thenReturn(Optional.of(new OrganicaDTO(organicUnitId.toString(), "Direção Geral", "DG", null)));

    ListSiadapEvaluationsQuery q = new ListSiadapEvaluationsQuery(2026, null, null, "0", "20");

    ResponseEntity<WrapperSiadapEvaluationListDTO> response = handler.handle(q);

    assertEquals(200, response.getStatusCode().value());
    SiadapEvaluationDTO dto = response.getBody().getData().get(0);
    assertEquals("Maria Silva", dto.getEmployeeName());
    assertEquals("Direção Geral", dto.getOrganicUnitName());
    assertNotNull(dto.getLastUpdatedAt());
    assertEquals(1L, response.getBody().getTotalElements());
    verify(repository, never()).findByYear(anyString(), any(Pageable.class));
  }

  // totalElements reflete o resultado filtrado (Page devolvido pelo repository.findAll com o
  // Specification já aplicado), não o total do ano inteiro -- distinto de D-04, que prova a forma
  // do DTO; este prova o valor de paginação.
  @Test
  void handleReturnsTotalElementsFromFilteredPageNotFromWholeYear() {
    stubCriteriaMocks();

    // 3 elementos no total filtrado, mas só 2 cabem nesta página -- se o handler ainda lesse o
    // ano inteiro, o total devolvido seria outro valor, não 3.
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 2), 3));

    ListSiadapEvaluationsQuery q = new ListSiadapEvaluationsQuery(2026, "unit-1", "HARMONIZATION", "0", "2");

    ResponseEntity<WrapperSiadapEvaluationListDTO> response = handler.handle(q);

    assertEquals(3L, response.getBody().getTotalElements());
    assertEquals(2, response.getBody().getTotalPages());
    verify(repository, never()).findByYear(anyString(), any(Pageable.class));
  }

  // Critério 6 da Fase 104 -- sem estes três campos na listagem, deriveObjectiveNotifications no
  // frontend nunca anuncia uma proposta de objetivos por responder ao avaliado, porque decide por
  // acceptanceStatus, e esse campo nunca chegava pelo toDto privado desta query (só o
  // SiadapEvaluationMapper do detalhe o escrevia). Guarda de regressão do critério 6.
  @Test
  void listingDtoCarriesAcceptanceStatusDescriptionAndNegotiationComment() {
    UUID employeeId = UUID.randomUUID();

    SiadapEvaluationEntity entity = new SiadapEvaluationEntity();
    entity.setId(UUID.randomUUID());
    entity.setEmployeeId(employeeId.toString());
    entity.setYear("2026");
    entity.setEvaluationPhase("HARMONIZATION");
    entity.setValidatedQuota(false);
    entity.setAcceptanceStatus("NEGOTIATING");
    entity.setLastNegotiationComment("Peço menos peso nos resultados");

    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));
    when(funcionarioLookupPort.findById(employeeId)).thenReturn(Optional.empty());

    ListSiadapEvaluationsQuery q = new ListSiadapEvaluationsQuery(2026, null, null, "0", "20");

    ResponseEntity<WrapperSiadapEvaluationListDTO> response = handler.handle(q);

    SiadapEvaluationDTO dto = response.getBody().getData().get(0);
    assertEquals("NEGOTIATING", dto.getAcceptanceStatus());
    assertEquals("Em Negociação", dto.getAcceptanceStatusDesc());
    assertEquals("Peço menos peso nos resultados", dto.getLastNegotiationComment());
  }

  // Critério 6, caminho nulo -- estado da maioria das linhas em base: sem negociação nunca
  // iniciada, os três campos devem ficar a null, e fromCode (não fromCodeOrThrow) garante que a
  // listagem inteira não rebenta por um código de aceitação ausente.
  @Test
  void listingDtoLeavesAcceptanceFieldsNullWhenEntityHasNone() {
    UUID employeeId = UUID.randomUUID();

    SiadapEvaluationEntity entity = new SiadapEvaluationEntity();
    entity.setId(UUID.randomUUID());
    entity.setEmployeeId(employeeId.toString());
    entity.setYear("2026");
    entity.setEvaluationPhase("HARMONIZATION");
    entity.setValidatedQuota(false);
    entity.setAcceptanceStatus(null);
    entity.setLastNegotiationComment(null);

    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));
    when(funcionarioLookupPort.findById(employeeId)).thenReturn(Optional.empty());

    ListSiadapEvaluationsQuery q = new ListSiadapEvaluationsQuery(2026, null, null, "0", "20");

    ResponseEntity<WrapperSiadapEvaluationListDTO> response = handler.handle(q);

    SiadapEvaluationDTO dto = response.getBody().getData().get(0);
    assertEquals(null, dto.getAcceptanceStatus());
    assertEquals(null, dto.getAcceptanceStatusDesc());
    assertEquals(null, dto.getLastNegotiationComment());
  }
}

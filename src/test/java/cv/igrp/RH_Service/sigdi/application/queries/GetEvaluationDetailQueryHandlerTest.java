package cv.igrp.RH_Service.sigdi.application.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * Primeira suite deste handler (112-02-PLAN.md, Task 2) -- não existia antes. Fecha a segunda
 * metade de LIG-03: {@link GetEvaluationDetailQueryHandler} alimenta o PDF de exportação
 * (siadap-evaluation-pdf.ts:125, coluna "Avaliador"), e nunca escreveu evaluatorName.
 * <p>
 * O handler compõe {@code mapper.toDto(mapper.toEntity(evaluation))} -- {@link #stubMapperRoundTrip}
 * estabiliza essa composição sem exercitar o mapper real, reproduzindo apenas o que
 * {@code SiadapEvaluationMapper.toDto} já faz na linha 118 (escrever evaluatorId no DTO).
 */
@ExtendWith(MockitoExtension.class)
class GetEvaluationDetailQueryHandlerTest {

  @Mock
  private SiadapEvaluationRepository evaluationRepository;

  @Mock
  private FuncionarioLookupPort funcionarioLookupPort;

  @Mock
  private OrganicaLookupPort organicaLookupPort;

  @Mock
  private SiadapEvaluationMapper mapper;

  @InjectMocks
  private GetEvaluationDetailQueryHandler handler;

  /**
   * Estabiliza mapper.toEntity/mapper.toDto para a instância de domínio dada, reproduzindo
   * apenas o que o mapper real escreve nesse round-trip (id, employeeId, evaluatorId) -- não
   * exercita SiadapEvaluationMapper.toDto/toEntity reais.
   */
  private SiadapEvaluationDTO stubMapperRoundTrip(SiadapEvaluation evaluation) {
    SiadapEvaluationEntity entity = new SiadapEvaluationEntity();
    when(mapper.toEntity(evaluation)).thenReturn(entity);

    SiadapEvaluationDTO dto = new SiadapEvaluationDTO();
    dto.setId(evaluation.getId().getStringValor());
    dto.setEmployeeId(evaluation.getEmployeeId());
    dto.setEvaluatorId(evaluation.getEvaluatorId());
    when(mapper.toDto(entity)).thenReturn(dto);

    return dto;
  }

  // LIG-03, caminho resolvido -- employeeId e evaluatorId, ambos presentes e distintos, são
  // resolvidos para os nomes certos e não trocados entre si: o risco real é inverter os dois
  // identificadores no lookup, e um teste que só olhasse para um dos dois não o apanharia.
  @Test
  void handleResolvesEvaluatorNameAlongsideEmployeeNameWithoutSwappingThem() {
    UUID employeeId = UUID.randomUUID();
    UUID evaluatorId = UUID.randomUUID();

    SiadapEvaluation evaluation = SiadapEvaluation.create(employeeId.toString(), 2026, null,
        evaluatorId.toString(), new BigDecimal("60"), new BigDecimal("40"));

    when(evaluationRepository.findById(any(SiadapEvaluationId.class)))
        .thenReturn(Optional.of(evaluation));
    stubMapperRoundTrip(evaluation);
    when(funcionarioLookupPort.findById(employeeId))
        .thenReturn(Optional.of(new FuncionarioDTO(employeeId.toString(), "Maria Silva")));
    when(funcionarioLookupPort.findById(evaluatorId))
        .thenReturn(Optional.of(new FuncionarioDTO(evaluatorId.toString(), "João Santos")));

    GetEvaluationDetailQuery query = new GetEvaluationDetailQuery(evaluation.getId().getStringValor());

    ResponseEntity<SiadapEvaluationDTO> response = handler.handle(query);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Maria Silva", response.getBody().getEmployeeName());
    assertEquals("João Santos", response.getBody().getEvaluatorName());
  }

  // LIG-03, caminho nulo (D-05 da Fase 109) -- evaluatorId nulo não pode chegar a
  // UUID.fromString nem ao porto de lookup. Reforçado desde a primeira escrita com o mesmo mock
  // estático que 112-01-SUMMARY.md documentou como necessário: uma asserção só sobre a contagem
  // de chamadas a funcionarioLookupPort.findById NÃO distingue a guarda de um try/catch que
  // apanhe a NullPointerException de UUID.fromString(null) -- essa exceção ocorre antes de
  // findById ser sequer invocado, tornando o número de chamadas ao porto idêntico com ou sem
  // guarda. A asserção decisiva verifica diretamente quantas vezes UUID.fromString é invocado:
  // exatamente 2 -- SiadapEvaluationId.from(query.getEvaluationId()), no arranque do handle, e o
  // lookup do employeeId -- nenhuma terceira chamada para o evaluatorId nulo. Via mock estático
  // que delega para a implementação real (a resolução de evaluationId/employeeId continua
  // genuína).
  @Test
  void handleLeavesEvaluatorNameNullAndNeverParsesEvaluatorIdWhenItIsNull() {
    UUID employeeId = UUID.randomUUID();

    SiadapEvaluation evaluation = SiadapEvaluation.create(employeeId.toString(), 2026, null,
        null, new BigDecimal("60"), new BigDecimal("40"));

    when(evaluationRepository.findById(any(SiadapEvaluationId.class)))
        .thenReturn(Optional.of(evaluation));
    stubMapperRoundTrip(evaluation);
    when(funcionarioLookupPort.findById(employeeId))
        .thenReturn(Optional.of(new FuncionarioDTO(employeeId.toString(), "Maria Silva")));

    GetEvaluationDetailQuery query = new GetEvaluationDetailQuery(evaluation.getId().getStringValor());

    try (MockedStatic<UUID> uuidStatic = Mockito.mockStatic(UUID.class, Mockito.CALLS_REAL_METHODS)) {
      ResponseEntity<SiadapEvaluationDTO> response = handler.handle(query);

      assertEquals(200, response.getStatusCode().value());
      assertNull(response.getBody().getEvaluatorName());
      verify(funcionarioLookupPort, times(1)).findById(any(UUID.class));
      verify(funcionarioLookupPort, times(1)).findById(employeeId);
      verify(funcionarioLookupPort, never()).findById(null);
      // Decisivo: exatamente 2 chamadas totais (evaluationId + employeeId), nenhuma para o
      // evaluatorId nulo.
      uuidStatic.verify(() -> UUID.fromString(any()), times(2));
    }
  }

  // LIG-03, avaliação inexistente -- guarda o caminho de falha do handler, que também nunca
  // teve teste. O lookup do avaliador (e do avaliado) nunca deve ser tentado antes de a
  // avaliação existir.
  @Test
  void handleThrowsNotFoundAndNeverLooksUpFuncionarioWhenEvaluationIsAbsent() {
    when(evaluationRepository.findById(any(SiadapEvaluationId.class)))
        .thenReturn(Optional.empty());

    GetEvaluationDetailQuery query = new GetEvaluationDetailQuery(UUID.randomUUID().toString());

    IgrpResponseStatusException exception =
        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(query));

    assertEquals(404, exception.getBody().getStatus());
    verify(funcionarioLookupPort, never()).findById(any(UUID.class));
    verify(organicaLookupPort, never()).findById(any(UUID.class));
  }
}

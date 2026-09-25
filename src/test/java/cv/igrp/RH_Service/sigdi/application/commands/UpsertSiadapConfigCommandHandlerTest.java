package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapConfigRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapConfigResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.admin.models.SiadapConfig;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.SiadapConfigRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UpsertSiadapConfigCommandHandlerTest {

  @Mock
  private SiadapConfigRepository configRepository;

  @InjectMocks
  private UpsertSiadapConfigCommandHandler handler;

  private SiadapConfigRequestDTO createValidRequest() {
    SiadapConfigRequestDTO req = new SiadapConfigRequestDTO();
    req.setGoodScore(new BigDecimal("3.5"));
    req.setExcellentScore(new BigDecimal("4.5"));
    req.setGoodQuota(new BigDecimal("30"));
    req.setExcellentQuota(new BigDecimal("20"));
    req.setMinimumCollaboratorsForQuota(10);
    req.setResultsWeight(new BigDecimal("60"));
    req.setCompetenciesWeight(new BigDecimal("40"));
    return req;
  }

  @Test
  void successfulUpsertNewConfig() {
    SiadapConfigRequestDTO req = createValidRequest();
    UpsertSiadapConfigCommand command = new UpsertSiadapConfigCommand(2026, req);

    when(configRepository.findByFiscalYear(2026)).thenReturn(Optional.empty());
    when(configRepository.save(any(SiadapConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

    ResponseEntity<SiadapConfigResponseDTO> response = handler.handle(command);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2026, response.getBody().getYear());
    assertEquals(new BigDecimal("3.5"), response.getBody().getGoodScore());
    assertEquals(new BigDecimal("4.5"), response.getBody().getExcellentScore());
    assertEquals(new BigDecimal("30"), response.getBody().getGoodQuota());
    assertEquals(new BigDecimal("20"), response.getBody().getExcellentQuota());
    verify(configRepository).save(any(SiadapConfig.class));
  }

  @Test
  void successfulUpsertExistingConfig() {
    SiadapConfigRequestDTO req = createValidRequest();
    UpsertSiadapConfigCommand command = new UpsertSiadapConfigCommand(2026, req);

    SiadapConfig existing = SiadapConfig.create(
        2026,
        new BigDecimal("3.0"),
        new BigDecimal("4.0"),
        new BigDecimal("15"),
        new BigDecimal("25"),
        5,
        new BigDecimal("50"),
        new BigDecimal("50")
    );

    when(configRepository.findByFiscalYear(2026)).thenReturn(Optional.of(existing));
    when(configRepository.save(any(SiadapConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

    ResponseEntity<SiadapConfigResponseDTO> response = handler.handle(command);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(new BigDecimal("3.5"), response.getBody().getGoodScore());
    assertEquals(new BigDecimal("4.5"), response.getBody().getExcellentScore());
    verify(configRepository).save(any(SiadapConfig.class));
  }

  @Test
  void refusesWhenWeightsDoNotSum100() {
    SiadapConfigRequestDTO req = createValidRequest();
    req.setResultsWeight(new BigDecimal("70"));
    req.setCompetenciesWeight(new BigDecimal("40")); // Sum = 110 != 100
    UpsertSiadapConfigCommand command = new UpsertSiadapConfigCommand(2026, req);

    IgrpResponseStatusException ex = assertThrows(
        IgrpResponseStatusException.class,
        () -> handler.handle(command)
    );

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals("A soma dos pesos de resultados e competências deve ser exatamente 100%", ex.getBody().getTitle());
  }

  @Test
  void refusesWhenExcellentScoreLessThanOrEqualToGoodScore_GAP_01() {
    SiadapConfigRequestDTO req = createValidRequest();
    req.setGoodScore(new BigDecimal("4.5"));
    req.setExcellentScore(new BigDecimal("3.5")); // Inversion!
    UpsertSiadapConfigCommand command = new UpsertSiadapConfigCommand(2026, req);

    IgrpResponseStatusException ex = assertThrows(
        IgrpResponseStatusException.class,
        () -> handler.handle(command)
    );

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals("A nota mínima de Excelente deve ser superior à nota mínima de Bom", ex.getBody().getTitle());

    // Also test equal scores
    req.setExcellentScore(new BigDecimal("4.5")); // Equal!
    IgrpResponseStatusException exEqual = assertThrows(
        IgrpResponseStatusException.class,
        () -> handler.handle(command)
    );
    assertEquals(HttpStatus.BAD_REQUEST, exEqual.getStatusCode());
    assertEquals("A nota mínima de Excelente deve ser superior à nota mínima de Bom", exEqual.getBody().getTitle());
  }

  @Test
  void refusesWhenCumulativeQuotasExceed100_GAP_02() {
    SiadapConfigRequestDTO req = createValidRequest();
    req.setGoodQuota(new BigDecimal("60"));
    req.setExcellentQuota(new BigDecimal("45")); // Sum = 105 > 100
    UpsertSiadapConfigCommand command = new UpsertSiadapConfigCommand(2026, req);

    IgrpResponseStatusException ex = assertThrows(
        IgrpResponseStatusException.class,
        () -> handler.handle(command)
    );

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals("A soma das quotas de Bom e Excelente não pode exceder 100%", ex.getBody().getTitle());
  }

  @Test
  void refusesWhenQuotaIsNegative_GAP_02() {
    SiadapConfigRequestDTO req = createValidRequest();
    req.setGoodQuota(new BigDecimal("-5"));
    UpsertSiadapConfigCommand command = new UpsertSiadapConfigCommand(2026, req);

    IgrpResponseStatusException ex = assertThrows(
        IgrpResponseStatusException.class,
        () -> handler.handle(command)
    );

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals("As quotas de diferenciação de desempenho não podem ser negativas", ex.getBody().getTitle());
  }
}

package cv.igrp.RH_Service.sigdi.infrastructure.client;

import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;
import cv.igrp.RH_Service.sigdi.application.port.EconomicClassifierPort;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class EconomicClassifierClientMock implements EconomicClassifierPort {

  // tabela de budgets fictícios por rubrica
  private static final Map<String, BigDecimal> BUDGETS = Map.of(
      "01.01.01", new BigDecimal("50000.00"),
      "01.01.02", new BigDecimal("120000.00"),
      "02.03.01", new BigDecimal("8000.00"),
      "03.05.10", new BigDecimal("0.00")
  );

  @Override
  public BudgetInfoDTO getBudget(String economicClassifier) {
    log.info("[MOCK] EconomicClassifier getBudget called for: {}", economicClassifier);

    if (!BUDGETS.containsKey(economicClassifier)) {
      // lista todas as rubricas conhecidas
      Set<String> availableRubrics = BUDGETS.keySet();
      throw IgrpResponseStatusException.notFound(
          "Rubrica econômica não encontrada: " + economicClassifier +
              ". Rubricas disponíveis: " + availableRubrics
      );
    }

    BigDecimal available = BUDGETS.get(economicClassifier);
    return new BudgetInfoDTO(economicClassifier, available);
  }
}

package cv.igrp.RH_Service.sigdi.application.port;

import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;

public interface EconomicClassifierPort {
  BudgetInfoDTO getBudget(String economicClassifier);
}

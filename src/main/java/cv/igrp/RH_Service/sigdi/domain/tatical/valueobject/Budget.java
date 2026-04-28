package cv.igrp.RH_Service.sigdi.domain.tatical.valueobject;

import cv.igrp.RH_Service.sigdi.domain.shared.valueobject.EconomicClassifier;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Budget {

  private final BigDecimal estimatedAmount;
  private final EconomicClassifier classifier;

  private Budget(BigDecimal estimatedAmount, EconomicClassifier classifier) {
    if (estimatedAmount == null) {
      throw new IllegalArgumentException("estimatedAmount é obrigatório");
    }
    if (estimatedAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("estimatedAmount deve ser maior que zero");
    }
    if (classifier == null) {
      throw new IllegalArgumentException("classifier é obrigatório");
    }
    this.estimatedAmount = estimatedAmount;
    this.classifier = classifier;
  }

  public static Budget of(BigDecimal estimatedAmount, EconomicClassifier classifier) {
    return new Budget(estimatedAmount, classifier);
  }

  public static Budget of(BigDecimal estimatedAmount, String classifierCode) {
    return new Budget(estimatedAmount, EconomicClassifier.of(classifierCode));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Budget that)) return false;
    return estimatedAmount.compareTo(that.estimatedAmount) == 0 &&
        classifier.equals(that.classifier);
  }

  @Override
  public int hashCode() {
    return 31 * estimatedAmount.hashCode() + classifier.hashCode();
  }

  @Override
  public String toString() {
    return estimatedAmount + " [" + classifier + "]";
  }
}

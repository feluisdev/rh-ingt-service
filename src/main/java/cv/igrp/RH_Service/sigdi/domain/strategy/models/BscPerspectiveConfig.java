package cv.igrp.RH_Service.sigdi.domain.strategy.models;

import lombok.Getter;

import java.util.UUID;

/**
 * Domain model for a single BSC perspective's admin-editable label and display order.
 * <p>
 * Rows are Flyway-seeded (V26) and fixed at exactly 4 -- one per internal code
 * (FINANCIAL/CUSTOMER/PROCESS/LEARNING). There is deliberately NO {@code create(...)} factory:
 * no operation above this model ever creates or deletes a row (CONTEXT.md: "sem operação
 * exposta de criar/apagar linha"). Only {@code label} and {@code displayOrder} are ever mutable;
 * {@code id} and {@code code} are immutable for the lifetime of a row.
 */
@Getter
public class BscPerspectiveConfig {

  private final UUID id;
  private final String code;
  private final String label;
  private final Integer displayOrder;

  private BscPerspectiveConfig(UUID id, String code, String label, Integer displayOrder) {
    if (code == null || code.isBlank()) throw new IllegalArgumentException("code é obrigatório");
    if (label == null || label.isBlank()) throw new IllegalArgumentException("label é obrigatório");
    if (displayOrder == null) throw new IllegalArgumentException("displayOrder é obrigatório");
    this.id = id;
    this.code = code;
    this.label = label;
    this.displayOrder = displayOrder;
  }

  public static BscPerspectiveConfig reconstruct(UUID id, String code, String label, Integer displayOrder) {
    return new BscPerspectiveConfig(id, code, label, displayOrder);
  }

  public BscPerspectiveConfig update(String label, Integer displayOrder) {
    return new BscPerspectiveConfig(this.id, this.code, label, displayOrder);
  }
}

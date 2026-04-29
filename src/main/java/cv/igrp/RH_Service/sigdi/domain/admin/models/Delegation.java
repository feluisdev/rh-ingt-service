package cv.igrp.RH_Service.sigdi.domain.admin.models;

import cv.igrp.RH_Service.sigdi.domain.admin.valueobject.DelegationId;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class Delegation {

  private final DelegationId id;
  private final UUID institutionId;
  private final UUID delegatorId;
  private final UUID delegateId;
  private final String scope;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final String reason;
  private final boolean active;

  private Delegation(DelegationId id, UUID institutionId, UUID delegatorId, UUID delegateId,
                     String scope, LocalDate startDate, LocalDate endDate, String reason, boolean active) {
    if (delegatorId == null) throw new IllegalArgumentException("delegatorId é obrigatório");
    if (delegateId == null) throw new IllegalArgumentException("delegateId é obrigatório");
    if (delegatorId.equals(delegateId))
      throw new IllegalArgumentException("delegatorId e delegateId não podem ser iguais");
    if (scope == null || scope.isBlank()) throw new IllegalArgumentException("scope é obrigatório");
    if (startDate == null) throw new IllegalArgumentException("startDate é obrigatório");
    if (endDate == null) throw new IllegalArgumentException("endDate é obrigatório");
    if (endDate.isBefore(startDate))
      throw new IllegalArgumentException("endDate não pode ser anterior ao startDate");

    this.id = id;
    this.institutionId = institutionId;
    this.delegatorId = delegatorId;
    this.delegateId = delegateId;
    this.scope = scope;
    this.startDate = startDate;
    this.endDate = endDate;
    this.reason = reason;
    this.active = active;
  }

  public static Delegation create(UUID institutionId, UUID delegatorId, UUID delegateId,
                                   String scope, LocalDate startDate, LocalDate endDate, String reason) {
    return new Delegation(DelegationId.gerarNovo(), institutionId, delegatorId, delegateId,
        scope, startDate, endDate, reason, true);
  }

  public static Delegation reconstruct(DelegationId id, UUID institutionId, UUID delegatorId,
                                        UUID delegateId, String scope, LocalDate startDate,
                                        LocalDate endDate, String reason, boolean active) {
    return new Delegation(id, institutionId, delegatorId, delegateId, scope, startDate, endDate, reason, active);
  }

  /**
   * Returns a new Delegation instance with active = false (revoked state).
   */
  public Delegation revoke() {
    return new Delegation(this.id, this.institutionId, this.delegatorId, this.delegateId,
        this.scope, this.startDate, this.endDate, this.reason, false);
  }
}

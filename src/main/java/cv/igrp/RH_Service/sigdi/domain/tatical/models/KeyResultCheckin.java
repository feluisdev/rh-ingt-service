package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultCheckinId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class KeyResultCheckin {

  private final KeyResultCheckinId id;
  private final KeyResultId keyResultId;
  private final BigDecimal valueAdded;
  private final String evidenceUrl;
  private final String comment;
  private final LocalDateTime checkinDate;

  private KeyResultCheckin(KeyResultCheckinId id, KeyResultId keyResultId, BigDecimal valueAdded,
                           String evidenceUrl, String comment, LocalDateTime checkinDate) {

    if (valueAdded == null || valueAdded.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("valueAdded é obrigatório e deve ser maior que zero");
    }
    if (comment == null || comment.isBlank()) {
      throw new IllegalArgumentException("comment é obrigatório");
    }
    if (keyResultId == null) {
      throw new IllegalArgumentException("keyResultId é obrigatório");
    }
    this.id = id;
    this.keyResultId = keyResultId;
    this.valueAdded = valueAdded;
    this.evidenceUrl = evidenceUrl;
    this.comment = comment;
    this.checkinDate = (checkinDate != null) ? checkinDate : LocalDateTime.now();
  }

  public static KeyResultCheckin create(KeyResultId keyResultId, BigDecimal valueAdded,
                                        String evidenceUrl, String comment) {
    return new KeyResultCheckin(
        KeyResultCheckinId.gerarNovo(),
        keyResultId,
        valueAdded,
        evidenceUrl,
        comment,
        LocalDateTime.now()
    );
  }

  public static KeyResultCheckin reconstruct(KeyResultCheckinId id, KeyResultId keyResultId,
                                             BigDecimal valueAdded, String evidenceUrl,
                                             String comment, LocalDateTime checkinDate) {
    return new KeyResultCheckin(id, keyResultId, valueAdded, evidenceUrl, comment, checkinDate);
  }
}

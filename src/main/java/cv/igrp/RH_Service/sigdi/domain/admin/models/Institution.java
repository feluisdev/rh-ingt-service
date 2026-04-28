package cv.igrp.RH_Service.sigdi.domain.admin.models;

import cv.igrp.RH_Service.sigdi.domain.admin.valueobject.InstitutionId;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class Institution {

  private final InstitutionId id;
  private final String code;
  private final String name;
  private final String type;
  private final boolean active;
  private final OffsetDateTime deactivatedAt;
  private final String contactEmail;

  private Institution(InstitutionId id, String code, String name, String type, boolean active,
                      OffsetDateTime deactivatedAt, String contactEmail) {
    if (id == null) throw new IllegalArgumentException("id é obrigatório");
    if (code == null || code.isBlank()) throw new IllegalArgumentException("code é obrigatório");
    if (name == null || name.isBlank()) throw new IllegalArgumentException("name é obrigatório");
    if (type == null || type.isBlank()) throw new IllegalArgumentException("type é obrigatório");
    this.id = id;
    this.code = code;
    this.name = name;
    this.type = type;
    this.active = active;
    this.deactivatedAt = deactivatedAt;
    this.contactEmail = contactEmail;
  }

  public static Institution create(String code, String name, String type, String contactEmail) {
    return new Institution(InstitutionId.gerarNovo(), code, name, type, true, null, contactEmail);
  }

  public static Institution reconstruct(InstitutionId id, String code, String name, String type,
                                         boolean active, OffsetDateTime deactivatedAt, String contactEmail) {
    return new Institution(id, code, name, type, active, deactivatedAt, contactEmail);
  }

  public Institution deactivate() {
    return new Institution(this.id, this.code, this.name, this.type, false,
        OffsetDateTime.now(), this.contactEmail);
  }
}

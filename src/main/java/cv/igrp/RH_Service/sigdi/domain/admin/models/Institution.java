package cv.igrp.RH_Service.sigdi.domain.admin.models;

import cv.igrp.RH_Service.sigdi.domain.admin.valueobject.InstitutionId;
import lombok.Getter;

@Getter
public class Institution {

  private final InstitutionId id;
  private final String code;
  private final String name;
  private final String type;
  private final boolean active;

  private Institution(InstitutionId id, String code, String name, String type, boolean active) {
    if (id == null) throw new IllegalArgumentException("id é obrigatório");
    if (code == null || code.isBlank()) throw new IllegalArgumentException("code é obrigatório");
    if (name == null || name.isBlank()) throw new IllegalArgumentException("name é obrigatório");
    if (type == null || type.isBlank()) throw new IllegalArgumentException("type é obrigatório");
    this.id = id;
    this.code = code;
    this.name = name;
    this.type = type;
    this.active = active;
  }

  public static Institution create(String code, String name, String type) {
    return new Institution(InstitutionId.gerarNovo(), code, name, type, true);
  }

  public static Institution reconstruct(InstitutionId id, String code, String name, String type, boolean active) {
    return new Institution(id, code, name, type, active);
  }

  public Institution deactivate() {
    return new Institution(this.id, this.code, this.name, this.type, false);
  }
}

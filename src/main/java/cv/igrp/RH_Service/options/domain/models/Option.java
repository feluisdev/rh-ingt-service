package cv.igrp.RH_Service.options.domain.models;

import cv.igrp.RH_Service.options.domain.valueobject.Metadata;
import cv.igrp.RH_Service.options.domain.valueobject.OptionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class Option {

  private final OptionId id;
  private String ccode;
  private String ckey;
  private String cvalue;
  private String locale = "pt-CV";
  private Integer sort_order;
  private Boolean active = true;
  private Metadata metadata; // JSON como String
  private String description;


  private Option(OptionId id,
                 String ccode,
                 String ckey,
                 String cvalue,
                 String locale,
                 Integer sortOrder,
                 Boolean active,
                 Metadata metadata,
                 String description) {

    this.id = Objects.requireNonNull(id, "Identificador não pode ser nulo");
    this.ccode = Objects.requireNonNull(ccode, "Código do conjunto não pode ser nulo");
    this.ckey = Objects.requireNonNull(ckey, "Chave da opção não pode ser nula");
    this.cvalue = Objects.requireNonNull(cvalue, "Valor da opção não pode ser nulo");
    this.locale = (locale != null && !locale.isBlank()) ? locale : "pt-CV";
    this.sort_order = sortOrder;
    this.active = (active != null) ? active : true;
    this.metadata = metadata;
    this.description = description;
  }

  /**
   * Criar uma nova Option
   */
  public static Option create(String ccode,
                              String ckey,
                              String cvalue,
                              String locale,
                              Integer sortOrder,
                              Metadata metadata,
                              String description) {

    return new Option(
        OptionId.gerarNovo(),
        ccode,
        ckey,
        cvalue,
        locale,
        sortOrder,
        true, // Ativo por padrão
        metadata,
        description
    );
  }

  /**
   * Reconstruir uma Option já existente (ex: vindo da base de dados)
   */
  public static Option rebuild(OptionId id,
                               String ccode,
                               String ckey,
                               String cvalue,
                               String locale,
                               Integer sortOrder,
                               Boolean active,
                               Metadata metadata,
                               String description) {

    return new Option(id, ccode, ckey, cvalue, locale, sortOrder, active, metadata, description);
  }

  /**
   * Atualizar atributos principais
   */
  public void update(String ccode,
                     String ckey,
                     String cvalue,
                     String locale,
                     Integer sortOrder,
                     Metadata metadata,
                     String description) {

    this.ccode = Objects.requireNonNull(ccode, "Código do conjunto não pode ser nulo");
    this.ckey = Objects.requireNonNull(ckey, "Chave da opção não pode ser nula");
    this.cvalue = Objects.requireNonNull(cvalue, "Valor da opção não pode ser nulo");
    this.locale = (locale != null && !locale.isBlank()) ? locale : "pt-CV";
    this.sort_order = sortOrder;
    this.metadata = metadata;
    this.description = description;
  }

  /**
   * Ativar Option
   */
  public void enable() {
    this.active = true;
  }

  /**
   * Desativar Option
   */
  public void disable() {
    this.active = false;
  }

  public boolean isEnabled() {
    return active != null && active;
  }

  private static void isValid(String ccode, String ckey) {
    List<String> errors = new ArrayList<>();

    // Validação regex ccode
    if (!ccode.matches("^[A-Z][A-Z0-9_]*$")) {
      errors.add("Código deve começar com letra e conter apenas letras maiúsculas, números e underscore");
    }

    // Validação regex ckey
    if (!ckey.matches("^[A-Z][A-Z0-9_]*$")) {
      errors.add("Chave deve começar com letra e conter apenas letras maiúsculas, números e underscore");
    }

    // Validação de comprimento
    if (ccode.length() > 50) {
      errors.add("Código não pode ter mais de 50 caracteres");
    }

    if (!errors.isEmpty()) {
      throw IgrpResponseStatusException.badRequest(String.join("; ", errors));
    }
  }


}

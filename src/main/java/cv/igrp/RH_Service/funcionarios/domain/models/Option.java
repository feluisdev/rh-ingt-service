package cv.igrp.RH_Service.funcionarios.domain.models;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Option {

  private ExternalID idOption;
  private String ccode;
  private String ckey;
  private String cvalue;
  private String locale;
  private Integer sortOrder;
  private boolean active;
  private String description;

  private Option() {}

  private Option(ExternalID idOption,
                 String ccode,
                 String ckey,
                 String cvalue,
                 String locale,
                 Integer sortOrder,
                 boolean active,
                 String description) {
    this.idOption = idOption;
    this.ccode = ccode;
    this.ckey = ckey;
    this.cvalue = cvalue;
    this.locale = locale;
    this.sortOrder = sortOrder;
    this.active = active;
    this.description = description;
  }

  public static Option criar(String ccode,
                             String ckey,
                             String cvalue,
                             String locale,
                             Integer sortOrder,
                             boolean active,
                             String description) {
    return new Option(ExternalID.gerarNovo(), ccode, ckey, cvalue, locale, sortOrder, active, description);
  }

  public static Option reconstruir(ExternalID idOption,
                                   String ccode,
                                   String ckey,
                                   String cvalue,
                                   String locale,
                                   Integer sortOrder,
                                   boolean active,
                                   String description) {
    return new Option(idOption, ccode, ckey, cvalue, locale, sortOrder, active, description);
  }

  public void atualizar(String ccode,
                        String ckey,
                        String cvalue,
                        String locale,
                        Integer sortOrder,
                        boolean active,
                        String description) {
    this.ccode = ccode;
    this.ckey = ckey;
    this.cvalue = cvalue;
    this.locale = locale;
    this.sortOrder = sortOrder;
    this.active = active;
    this.description = description;
  }

  public void ativar() { this.active = true; }

  public void inativar() { this.active = false; }
}

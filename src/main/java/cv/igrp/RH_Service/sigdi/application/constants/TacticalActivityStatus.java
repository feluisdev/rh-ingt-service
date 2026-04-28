/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.constants;

import cv.igrp.framework.core.domain.IgrpEnum;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;


public enum TacticalActivityStatus implements IgrpEnum<String> {

  DRAFT("DRAFT", "Rascunho"),
    PENDING_BUDGET("PENDING_BUDGET", "Pendente de Orçamento"),
    PENDING_TACTICAL("PENDING_TACTICAL", "Pendente de aprovação tática"),
    PENDING_STRATEGIC("PENDING_STRATEGIC", "Pendente de aprovação estratégica"),
    APPROVED("APPROVED", "Aprovado"),
    REJECTED("REJECTED", "Rejeitado"),
    CANCELLED("CANCELLED", "Cancelado")
  ;

  private final String code;
  private final String description;

  TacticalActivityStatus(String code, String description) {
    this.code = code;
    this.description = description;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getDescription() {
    return description;
  }

  /**
  * Pre-built maps for fast lookup.
  */
  private static final Map<String, TacticalActivityStatus> CODE_MAP = Arrays.stream(values())
          .collect(Collectors.toMap(TacticalActivityStatus::getCode, Function.identity()));

  /**
  * Attempts to find the enum value associated with the given code.
  * @param code The code to look up
  * @return An Optional containing the enum value if found, empty Optional otherwise
  */
  public static Optional<TacticalActivityStatus> fromCode(String code) {
    return Optional.ofNullable(CODE_MAP.get(code));
  }

  /**
  * Finds the enum value associated with the given code or throws an exception if not found.
  * @param code The code to look up
  * @return The enum value for the given code
  * @throws IllegalArgumentException if no enum value exists for the given code
  */
  public static TacticalActivityStatus fromCodeOrThrow(String code) {
    return fromCode(code).orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST, "Invalid TacticalActivityStatus for this code: " + code));
  }

  /**
  * Returns a map of code to description.
  */
  public static Map<String, String> codeDescriptionMap() {
    return CODE_MAP.values().stream().collect(Collectors.toMap(TacticalActivityStatus::getCode, TacticalActivityStatus::getDescription));
  }

}

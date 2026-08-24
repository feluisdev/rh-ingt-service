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


public enum StrategicGoalsPerspective implements IgrpEnum<String> {

  FINANCIAL("FINANCIAL", "FINANCIAL"),
    CUSTOMER("CUSTOMER", "CUSTOMER"),
    LEARNING("LEARNING", "LEARNING"),
    PROCESS("PROCESS", "PROCESS")
  ;

  private final String code;
  private final String description;

  StrategicGoalsPerspective(String code, String description) {
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
  private static final Map<String, StrategicGoalsPerspective> CODE_MAP = Arrays.stream(values())
          .collect(Collectors.toMap(StrategicGoalsPerspective::getCode, Function.identity()));

  /**
  * Attempts to find the enum value associated with the given code.
  * @param code The code to look up
  * @return An Optional containing the enum value if found, empty Optional otherwise
  */
  public static Optional<StrategicGoalsPerspective> fromCode(String code) {
    return Optional.ofNullable(CODE_MAP.get(code));
  }

  /**
  * Finds the enum value associated with the given code or throws an exception if not found.
  * @param code The code to look up
  * @return The enum value for the given code
  * @throws IllegalArgumentException if no enum value exists for the given code
  */
  public static StrategicGoalsPerspective fromCodeOrThrow(String code) {
    return fromCode(code).orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST, "Código inválido para StrategicGoalsPerspective: " + code));
  }

  /**
  * Returns a map of code to description.
  */
  public static Map<String, String> codeDescriptionMap() {
    return CODE_MAP.values().stream().collect(Collectors.toMap(StrategicGoalsPerspective::getCode, StrategicGoalsPerspective::getDescription));
  }

}

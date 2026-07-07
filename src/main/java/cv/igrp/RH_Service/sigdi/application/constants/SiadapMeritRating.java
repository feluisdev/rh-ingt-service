package cv.igrp.RH_Service.sigdi.application.constants;

import cv.igrp.framework.core.domain.IgrpEnum;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;


/**
 * Menções qualitativas do SIADAP (Sistema Integrado de Avaliação do Desempenho da AP).
 * <p>
 * Baseado no DL 12/2020 de Cabo Verde e nas orientações do SIADAP 3 de Portugal,
 * com as seguintes menções ordenadas por desempenho crescente:
 * <ul>
 *   <li>INADEQUATE: 1.00 – 1.99 (Inadequado)</li>
 *   <li>REGULAR:    2.00 – 3.49 (Regular)</li>
 *   <li>GOOD:       3.50 – 3.99 (Bom)</li>
 *   <li>VERY_GOOD:  4.00 – 4.49 (Muito Bom)</li>
 *   <li>EXCELLENT:  4.50 – 5.00 (Excelente — sujeito a quota do CCA)</li>
 * </ul>
 */
public enum SiadapMeritRating implements IgrpEnum<String> {

  INADEQUATE("INADEQUATE", "Inadequado"),
  REGULAR("REGULAR", "Regular"),
  GOOD("GOOD", "Bom"),
  VERY_GOOD("VERY_GOOD", "Muito Bom"),
  EXCELLENT("EXCELLENT", "Excelente");

  private final String code;
  private final String description;

  SiadapMeritRating(String code, String description) {
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
  private static final Map<String, SiadapMeritRating> CODE_MAP = Arrays.stream(values())
          .collect(Collectors.toMap(SiadapMeritRating::getCode, Function.identity()));

  /**
  * Attempts to find the enum value associated with the given code.
  * @param code The code to look up
  * @return An Optional containing the enum value if found, empty Optional otherwise
  */
  public static Optional<SiadapMeritRating> fromCode(String code) {
    return Optional.ofNullable(CODE_MAP.get(code));
  }

  /**
  * Finds the enum value associated with the given code or throws an exception if not found.
  * @param code The code to look up
  * @return The enum value for the given code
  * @throws IllegalArgumentException if no enum value exists for the given code
  */
  public static SiadapMeritRating fromCodeOrThrow(String code) {
    return fromCode(code).orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST, "Invalid SiadapMeritRating for this code: " + code));
  }

  /**
  * Returns a map of code to description.
  */
  public static Map<String, String> codeDescriptionMap() {
    return CODE_MAP.values().stream().collect(Collectors.toMap(SiadapMeritRating::getCode, SiadapMeritRating::getDescription));
  }

}

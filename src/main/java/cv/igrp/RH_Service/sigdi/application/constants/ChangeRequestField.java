package cv.igrp.RH_Service.sigdi.application.constants;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Closed list of the fields a Change Request is allowed to alter on an APPROVED tactical
 * activity.
 *
 * <p>The codes are the exact strings the PAA Change Request modal sends
 * ({@code ChangeRequestModal.tsx}, frontend). Changing a code here without changing it there
 * turns every new request of that kind into a request that can never be approved -- which is
 * the defect this phase exists to close, reintroduced from the other side.
 *
 * <p>Until this enum existed the backend accepted any non-blank {@code fieldName}, so a typo
 * produced a request that looked valid and was silently inapplicable.
 */
public enum ChangeRequestField {

  BUDGET("budget", "Orçamento estimado"),
  START_DATE("start_date", "Data de início"),
  END_DATE("end_date", "Data de fim"),
  TITLE("title", "Título");

  private final String code;
  private final String description;

  ChangeRequestField(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  private static final Map<String, ChangeRequestField> CODE_MAP = Arrays.stream(values())
      .collect(Collectors.toMap(ChangeRequestField::getCode, Function.identity()));

  public static Optional<ChangeRequestField> fromCode(String code) {
    return (code == null) ? Optional.empty() : Optional.ofNullable(CODE_MAP.get(code.trim()));
  }

  /**
   * @throws IgrpResponseStatusException 400, naming the accepted codes -- a request whose field
   *     is unknown cannot be applied, and saying only "inválido" would leave the caller guessing.
   */
  public static ChangeRequestField fromCodeOrThrow(String code) {
    return fromCode(code).orElseThrow(() -> IgrpResponseStatusException.badRequest(
        "Campo de alteração não suportado: \"" + code + "\". Campos aceites: " + acceptedCodes()));
  }

  public static String acceptedCodes() {
    return Arrays.stream(values()).map(ChangeRequestField::getCode).collect(Collectors.joining(", "));
  }
}

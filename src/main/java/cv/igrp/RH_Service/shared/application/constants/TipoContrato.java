package cv.igrp.RH_Service.shared.application.constants;

import cv.igrp.framework.core.domain.IgrpEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum TipoContrato implements IgrpEnum<String> {

  TEMPO_INDETERMINADO("TEMPO_INDETERMINADO", "Contrato sem prazo definido"),
    PRAZO_CERTO("PRAZO_CERTO", "Contrato com prazo de início e fim definidos"),
    PRAZO_INCERTO("PRAZO_INCERTO", "Contrato com fim indefinido no início"),
    EXPERIENCIA("EXPERIENCIA", "Contrato em período experimental"),
    ESTAGIO("ESTAGIO", "Contrato de estágio"),
    TRABALHO_RURAL("TRABALHO_RURAL", "Contrato específico para atividades rurais"),
    OBRA_OU_SERVICO("OBRA_OU_SERVICO", "Contrato vinculado à execução de uma obra ou serviço específico"),
    TEMPO_PARCIAL("TEMPO_PARCIAL", "Contrato com jornada inferior à do tempo completo")
  ;

  private final String code;
  private final String description;

  TipoContrato(String code, String description) {
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
  private static final Map<String, TipoContrato> CODE_MAP = Arrays.stream(values())
          .collect(Collectors.toMap(TipoContrato::getCode, Function.identity()));

  /**
  * Attempts to find the enum value associated with the given code.
  * @param code The code to look up
  * @return An Optional containing the enum value if found, empty Optional otherwise
  */
  public static Optional<TipoContrato> fromCode(String code) {
    return Optional.ofNullable(CODE_MAP.get(code));
  }

  /**
  * Finds the enum value associated with the given code or throws an exception if not found.
  * @param code The code to look up
  * @return The enum value for the given code
  * @throws IllegalArgumentException if no enum value exists for the given code
  */
  public static TipoContrato fromCodeOrThrow(String code) {
    return fromCode(code).orElseThrow(() -> new IllegalArgumentException("Invalid TipoContrato for this code: " + code));
  }

  /**
  * Returns a map of code to description.
  */
  public static Map<String, String> codeDescriptionMap() {
    return CODE_MAP.values().stream().collect(Collectors.toMap(TipoContrato::getCode, TipoContrato::getDescription));
  }

}

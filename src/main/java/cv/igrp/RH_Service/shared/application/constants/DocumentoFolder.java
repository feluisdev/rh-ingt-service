/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.shared.application.constants;

import cv.igrp.framework.core.domain.IgrpEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum DocumentoFolder implements IgrpEnum<String> {

  FUNCIONARIO("funcionario_documents", "funcionario_documents"),
  DOCUMENTOS_EMITIDOS("documentos_emitidos", "documentos_emitidos"),
  /** Pasta por omissão: o ficheiro que não diz para onde vai (ou diz uma pasta que não existe). */
  OUTROS("outros", "outros")
  ;

  private final String code;
  private final String description;

  DocumentoFolder(String code, String description) {
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
  private static final Map<String, DocumentoFolder> CODE_MAP = Arrays.stream(values())
          .collect(Collectors.toMap(DocumentoFolder::getCode, Function.identity()));

  /**
  * Attempts to find the enum value associated with the given code.
  * @param code The code to look up
  * @return An Optional containing the enum value if found, empty Optional otherwise
  */
  public static Optional<DocumentoFolder> fromCode(String code) {
    return Optional.ofNullable(CODE_MAP.get(code));
  }

  /**
  * Finds the enum value associated with the given code or throws an exception if not found.
  * @param code The code to look up
  * @return The <enum> value for the given code
  * @throws IllegalArgumentException if no enum value exists for the given code
  */
  /**
  * A pasta pedida, por código ou por nome (sem distinguir maiúsculas); em branco ou desconhecida, {@link #OUTROS}.
  */
  public static DocumentoFolder ouOutros(String valor) {
    if (valor == null || valor.isBlank()) return OUTROS;
    String v = valor.trim();
    return fromCode(v.toLowerCase()).orElseGet(() -> Arrays.stream(values())
        .filter(f -> f.name().equalsIgnoreCase(v)).findFirst().orElse(OUTROS));
  }

  /** A pasta dada, ou {@link #OUTROS} se não vier nenhuma. */
  public static DocumentoFolder ouOutros(DocumentoFolder folder) {
    return folder != null ? folder : OUTROS;
  }

  public static DocumentoFolder fromCodeOrThrow(String code) {
    return fromCode(code).orElseThrow(() -> new IllegalArgumentException("Código inválido para DocumentoFolder: " + code));
  }

  /**
  * Returns a map of code to description.
  */
  public static Map<String, String> codeDescriptionMap() {
    return CODE_MAP.values().stream().collect(Collectors.toMap(DocumentoFolder::getCode, DocumentoFolder::getDescription));
  }

}

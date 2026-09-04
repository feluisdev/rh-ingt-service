package cv.igrp.RH_Service.shared.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class ApplicationAuditorAware implements AuditorAware<String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationAuditorAware.class);

  private static final String SYSTEM_FALLBACK = "system-bot@nosi.cv";

  /**
   * Resolve o auditor corrente para efeitos de {@code created_by}/{@code last_modified_by}.
   *
   * <p>Consulta primeiro {@link SystemAuditor#current()}. Um âmbito de sistema activo é uma
   * afirmação explícita de que a escrita em curso não é de nenhum utilizador — feita por um
   * agendador, por exemplo — e essa afirmação prevalece sobre o que estiver no
   * {@link SecurityContextHolder}, mesmo que por acaso lá esteja uma autenticação (residual de
   * uma thread reaproveitada). Só na ausência de um âmbito de sistema é que se segue o caminho
   * habitual: {@code sub} do JWT, depois {@code Authentication#getName()}, depois o fallback
   * genérico.
   */
  @Override
  public Optional<String> getCurrentAuditor() {
    Optional<String> systemAuditor = SystemAuditor.current();
    if (systemAuditor.isPresent()) {
      return systemAuditor;
    }
    return Optional.ofNullable(getCurrentSubjectName()).filter(s -> !s.isBlank());
  }

  /**
   * Resolves the current user identity for auditing purposes.
   * Priority:
   * 1) sub claim from JWT if present
   * 2) Authentication#getName() if an Authentication exists
   * 3) Fallback to system account for background processing
   */
  private String getCurrentSubjectName() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();
      String sub = jwt.getClaimAsString("sub");
      if (sub != null && !sub.isBlank()) {
        LOGGER.debug("Resolved auditor from JWT sub: {}", sub);
        return sub;
      }
    }

    if (authentication != null) {
      String name = authentication.getName();
      if (name != null && !name.isBlank()) {
        LOGGER.debug("Resolved auditor from authentication name: {}", name);
        return name;
      }
    }

    LOGGER.warn("No authenticated user found, falling back to system account");
    return SYSTEM_FALLBACK;
  }

}

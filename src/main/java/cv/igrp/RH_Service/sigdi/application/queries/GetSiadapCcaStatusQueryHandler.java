package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.security.SecurityMode;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapCcaStatusDTO;
import cv.igrp.framework.auth.core.security.IgrpAuthorizationService;
import cv.igrp.framework.auth.generated.PermissionsRegistry.Permission;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * (a) This is the only read point for CCA (Conselho Coordenador da Avaliacao) membership,
 * created by decision D-01 of Phase 104: a dedicated read endpoint, not a deduction from a
 * write attempt's 403.
 *
 * <p>(b) In production, the decision lives in the authenticated caller's authorities, read directly
 * from {@link IgrpAuthorizationService#checkPermission(Permission)} for
 * {@code siadap.cca.consultarEstado}. In non-production/development with security disabled,
 * it supports the configured {@code SIADAP_CCA_EMPLOYEE_IDS} or resolved current employee
 * to allow development and QA verification without a Keycloak cluster.
 */
@Component
public class GetSiadapCcaStatusQueryHandler
    implements QueryHandler<GetSiadapCcaStatusQuery, ResponseEntity<SiadapCcaStatusDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetSiadapCcaStatusQueryHandler.class);

  private final IgrpAuthorizationService igrpAuthorization;
  private final CurrentEmployeeResolver currentEmployeeResolver;
  private final Environment environment;

  public GetSiadapCcaStatusQueryHandler(IgrpAuthorizationService igrpAuthorization) {
    this(igrpAuthorization, null, null);
  }

  @Autowired
  public GetSiadapCcaStatusQueryHandler(IgrpAuthorizationService igrpAuthorization,
                                        @Autowired(required = false) CurrentEmployeeResolver currentEmployeeResolver,
                                        @Autowired(required = false) Environment environment) {
    this.igrpAuthorization = igrpAuthorization;
    this.currentEmployeeResolver = currentEmployeeResolver;
    this.environment = environment;
  }

  @IgrpQueryHandler
  public ResponseEntity<SiadapCcaStatusDTO> handle(GetSiadapCcaStatusQuery query) {
    boolean isCca = igrpAuthorization.checkPermission(Permission.SIADAP_CCA_CONSULTARESTADO);
    if (!isCca && isSecurityDisabled() && currentEmployeeResolver != null) {
      try {
        var currentEmployeeId = currentEmployeeResolver.resolve();
        if (currentEmployeeId != null) {
          String defaultCcaIds = "91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901";
          String configuredCcaIds = environment != null
              ? environment.getProperty("SIADAP_CCA_EMPLOYEE_IDS", environment.getProperty("siadap.cca.employee-ids", defaultCcaIds))
              : defaultCcaIds;
          if (configuredCcaIds == null || configuredCcaIds.isBlank()) {
            configuredCcaIds = defaultCcaIds;
          }
          isCca = Arrays.asList(configuredCcaIds.split("[,;\\s]+"))
              .contains(currentEmployeeId.getValor().toString());
        }
      } catch (Exception e) {
        LOGGER.debug("Could not resolve current employee for dev CCA check: {}", e.getMessage());
      }
    }
    if (!isCca && isSecurityDisabled()) {
      isCca = true;
    }
    LOGGER.debug("GetSiadapCcaStatusQuery resolved isCca={}", isCca);
    return ResponseEntity.ok(new SiadapCcaStatusDTO(isCca));
  }

  private boolean isSecurityDisabled() {
    return environment != null && SecurityMode.isSecurityDisabled(environment);
  }
}

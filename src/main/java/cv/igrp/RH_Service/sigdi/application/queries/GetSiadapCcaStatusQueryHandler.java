package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.config.SiadapCcaSecurityProperties;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapCcaStatusDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * (a) This is the only read point for CCA (Conselho Coordenador da Avaliacao) membership,
 * created by decision D-01 of Phase 104: a dedicated read endpoint, not a deduction from a
 * write attempt's 403.
 *
 * <p>(b) The decision itself still lives entirely in {@link SiadapCcaSecurityProperties}. This
 * handler composes {@code currentEmployeeResolver.resolve()} with
 * {@code ccaSecurityProperties.isCca(...)} -- it does not replicate, invert, or cache any part
 * of that decision.
 *
 * <p>(c) The visibility this endpoint grants the frontend is NOT a security boundary. Who is a
 * CCA member continues to be verified server-side in
 * {@code AssignMeritRatingCommandHandler} and {@code CloseEvaluationsCommandHandler} (Phase 101),
 * and will continue to be even if a client forges this read's response.
 */
@Component
@RequiredArgsConstructor
public class GetSiadapCcaStatusQueryHandler
    implements QueryHandler<GetSiadapCcaStatusQuery, ResponseEntity<SiadapCcaStatusDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetSiadapCcaStatusQueryHandler.class);

  private final CurrentEmployeeResolver currentEmployeeResolver;
  private final SiadapCcaSecurityProperties ccaSecurityProperties;

  @IgrpQueryHandler
  public ResponseEntity<SiadapCcaStatusDTO> handle(GetSiadapCcaStatusQuery query) {
    String currentEmployeeId = currentEmployeeResolver.resolve().getStringValor();
    boolean isCca = ccaSecurityProperties.isCca(currentEmployeeId);
    LOGGER.debug("GetSiadapCcaStatusQuery resolved isCca={}", isCca);
    return ResponseEntity.ok(new SiadapCcaStatusDTO(isCca));
  }
}

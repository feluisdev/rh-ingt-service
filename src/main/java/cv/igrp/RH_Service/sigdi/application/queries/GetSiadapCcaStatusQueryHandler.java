package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.SiadapCcaStatusDTO;
import cv.igrp.framework.auth.core.security.IgrpAuthorizationService;
import cv.igrp.framework.auth.generated.PermissionsRegistry.Permission;
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
 * <p>(b) The decision itself now lives in the authenticated caller's authorities, read directly
 * from {@link IgrpAuthorizationService#checkPermission(Permission)} for
 * {@code siadap.cca.consultarEstado} -- not in {@code SiadapCcaSecurityProperties}, eliminated
 * by Phase 115/AUT-04. {@code CurrentEmployeeResolver} is gone from this handler for the same
 * reason: nothing here needs to know <em>who</em> the caller is anymore, only <em>what they can
 * do</em>. This handler does not replicate, invert, or cache any part of that decision -- it
 * asks and returns the answer.
 *
 * <p>(c) The visibility this endpoint grants the frontend is NOT a security boundary. Who is a
 * CCA member continues to be verified server-side by {@code @PreAuthorize} on
 * {@code ComplianceController#assignMeritRating} and {@code #closeEvaluations} (Phase 115), and
 * will continue to be even if a client forges this read's response.
 *
 * <p>(d) Why this is the only one of the five Phase 115 decision points without
 * {@code @PreAuthorize} on its controller method. The {@code 115-CONTEXT.md} plan for this phase
 * mapped this handler to {@code siadap.cca.consultarEstado} and called for the guard to move to
 * the controller, uniformly with the other four points -- but that collides with (a): putting
 * {@code @PreAuthorize} here would make {@code GET siadap/me/cca-status} answer {@code 403} to
 * anyone who is not a CCA member, which is exactly the deduction-from-a-403 that D-01 was
 * written to eliminate, and would make {@link SiadapCcaStatusDTO} degenerate -- only members
 * would ever receive the body, always {@code true}. Decided by the operator on 2026-08-27
 * (option {@code ler-a-permissao-no-handler}, see {@code 115-07-SUMMARY.md}): D-01 wins at this
 * one point. The endpoint stays open to any authenticated caller and answers from the
 * permission instead of guarding access to itself. One consequence of that choice:
 * {@code siadap.cca.consultarEstado} now designates <em>membership in the CCA</em>, not
 * <em>the right to consult it</em> -- every authenticated caller has the right to consult;
 * only members get {@code true} back. This is a reading of the caller's own state, not an
 * authorization decision; the decisions that gate what a CCA member can <em>do</em> are the
 * {@code @PreAuthorize} guards named in (c).
 */
@Component
@RequiredArgsConstructor
public class GetSiadapCcaStatusQueryHandler
    implements QueryHandler<GetSiadapCcaStatusQuery, ResponseEntity<SiadapCcaStatusDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetSiadapCcaStatusQueryHandler.class);

  private final IgrpAuthorizationService igrpAuthorization;

  @IgrpQueryHandler
  public ResponseEntity<SiadapCcaStatusDTO> handle(GetSiadapCcaStatusQuery query) {
    boolean isCca = igrpAuthorization.checkPermission(Permission.SIADAP_CCA_CONSULTARESTADO);
    LOGGER.debug("GetSiadapCcaStatusQuery resolved isCca={}", isCca);
    return ResponseEntity.ok(new SiadapCcaStatusDTO(isCca));
  }
}

package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query for {@code GET api/v1/compliance/siadap/me/cca-status} (SIA-05, decision D-01,
 * Phase 104). Carries no fields: since Phase 115/AUT-04 the response comes from the
 * authenticated caller's own authorities, read inside the handler via
 * {@code IgrpAuthorizationService}, never from a request parameter -- see
 * {@link GetSiadapCcaStatusQueryHandler}.
 *
 * <p>Deliberately no all-args-constructor annotation here: this class has no fields, so
 * combining {@code @NoArgsConstructor} with the Lombok annotation that generates a
 * constructor for every field would produce two colliding no-arg constructors, and the
 * class would not compile.
 */
@Data
@NoArgsConstructor
public class GetSiadapCcaStatusQuery implements Query {
}

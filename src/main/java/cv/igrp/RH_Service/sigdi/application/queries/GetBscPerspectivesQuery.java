package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * No-arg query: the BSC perspective configuration is global (no tenant/org/year dimension
 * to key by), so there is nothing to parameterize -- mirrors {@code GetJobsComboboxQuery}.
 */
@Getter
@RequiredArgsConstructor
public class GetBscPerspectivesQuery implements Query {
}

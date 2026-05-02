package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetMeLeaveRequestsQuery implements Query {
    private final String status;
    private final String leaveTypeId;
    private final Integer year;
}

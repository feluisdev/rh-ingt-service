package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetSubtiposLicencaMobilidadeQuery implements Query {
    private final Boolean active;
    private final String recordType;
}

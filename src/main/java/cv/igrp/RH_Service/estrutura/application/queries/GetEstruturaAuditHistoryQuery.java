package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetEstruturaAuditHistoryQuery implements Query {
    private final String catalog;
    private final String entityId;
}

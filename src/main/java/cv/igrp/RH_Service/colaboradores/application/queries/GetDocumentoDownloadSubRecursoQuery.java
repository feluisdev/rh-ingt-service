package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class GetDocumentoDownloadSubRecursoQuery implements Query {
    private final String referenceEntity;
    private final UUID referenceId;
    private final String documentoId;
}

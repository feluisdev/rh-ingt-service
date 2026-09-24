package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** As férias do ano do próprio ({@code /me}): preferência, marcação e alterações. */
@Getter
@RequiredArgsConstructor
public class GetMinhasFeriasQuery implements Query {
    private final int ano;
}

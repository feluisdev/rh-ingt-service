package cv.igrp.RH_Service.colaboradores.application.queries;

import java.time.LocalDate;
import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetMinhaAssiduidadeQuery implements Query {
    private final LocalDate de;
    private final LocalDate ate;
}

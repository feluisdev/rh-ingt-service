package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Doença prolongada: quem deve ir à junta médica (BR-SST-19) */
@Getter
@RequiredArgsConstructor
public class GetSugestoesJuntaMedicaQuery implements Query {
    private final String data;
}

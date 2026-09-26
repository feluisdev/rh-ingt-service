package cv.igrp.RH_Service.recrutamento.application.commands;

import cv.igrp.RH_Service.recrutamento.application.dto.CandidaturaRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** CANDIDATAR (sem candidaturaId), ADMITIR, EXCLUIR, AUDIENCIA, NOTA, PROVER ou DESISTIR. */
@Getter
@RequiredArgsConstructor
public class AccaoCandidaturaCommand implements Command {
    private final String concursoId;
    private final String candidaturaId;
    private final String accao;
    private final CandidaturaRequestDTO request;
}

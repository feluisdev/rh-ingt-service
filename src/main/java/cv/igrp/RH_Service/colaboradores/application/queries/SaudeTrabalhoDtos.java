package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ExameSaudeDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.JuntaMedicaDTO;
import cv.igrp.RH_Service.colaboradores.application.services.SaudeTrabalhoService;
import cv.igrp.RH_Service.colaboradores.domain.models.ExameSaude;
import cv.igrp.RH_Service.colaboradores.domain.models.JuntaMedica;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Dos exames e das juntas para os DTOs. */
@Component
@RequiredArgsConstructor
public class SaudeTrabalhoDtos {

    private final SaudeTrabalhoService service;

    public ExameSaudeDTO dto(ExameSaude e, List<String> alertas) {
        return new ExameSaudeDTO(e.getId().getStringValor(), e.getFuncionarioId().getStringValor(), service.nome(e.getFuncionarioId()),
                e.getTipo().name(), e.getData(), e.getEntidade(), e.getResultado().name(), e.getRestricoes(), e.getValidadeAte(),
                e.validoEm(LocalDate.now()), e.getObservacoes(), new ArrayList<>(alertas != null ? alertas : List.of()));
    }

    public JuntaMedicaDTO dto(JuntaMedica j, List<String> alertas) {
        return new JuntaMedicaDTO(j.getId().getStringValor(), j.getFuncionarioId().getStringValor(), service.nome(j.getFuncionarioId()),
                j.getMotivo().name(), j.getFundamentacao(), j.getDataPedido(), j.getDataJunta(), j.getParecer() != null ? j.getParecer().name() : null,
                j.getDiasIncapacidade(), j.getObservacoes(), j.getEstado().name(), new ArrayList<>(alertas != null ? alertas : List.of()));
    }
}

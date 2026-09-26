package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ExoneracaoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ExoneracaoService;
import cv.igrp.RH_Service.colaboradores.domain.models.Exoneracao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Da exoneração para o DTO, com as condicionantes de hoje (se não vierem, calculam-se). */
@Component
@RequiredArgsConstructor
public class ExoneracaoDtos {

    private final ExoneracaoService service;

    public ExoneracaoDTO dto(Exoneracao e, List<String> condicionantes) {
        List<String> cond = condicionantes != null ? condicionantes : e.emCurso() ? service.condicionantes(e.getFuncionarioId()) : List.of();
        return new ExoneracaoDTO(e.getId().getStringValor(), e.getFuncionarioId().getStringValor(), service.nome(e.getFuncionarioId()),
                e.getDataPreAviso(), e.getDataPretendida(), e.dataLimite(), e.getMotivo(), e.isPedidaPeloProprio(), e.getEstado().name(),
                e.getDespacho(), e.getDataDespacho(), e.getDataEfeito(), e.emCurso() || e.getDataEfeito() != null ? service.previsao(e, cond) : null,
                new ArrayList<>(cond));
    }
}

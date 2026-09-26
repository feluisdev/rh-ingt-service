package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.AcumulacaoFuncoesDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.AcumulacaoFuncoesRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AcumulacaoFuncoesService;
import cv.igrp.RH_Service.colaboradores.domain.models.AcumulacaoFuncoes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Da acumulação de funções para o DTO, e os termos vindos de fora. */
@Component
@RequiredArgsConstructor
public class AcumulacaoFuncoesDtos {

    private final AcumulacaoFuncoesService service;

    public AcumulacaoFuncoesDTO dto(AcumulacaoFuncoes a) {
        return new AcumulacaoFuncoesDTO(a.getId().getStringValor(), a.getFuncionarioId().getStringValor(), service.nome(a.getFuncionarioId()),
                a.getTipo().name(), a.getCasoPublico() != null ? a.getCasoPublico().name() : null, a.isRemunerada(), a.autorizacao().name(),
                a.getEntidade(), a.getFuncoes(), a.getHorario(), a.getHorasSemanais(), a.getInicio(), a.getFim(), a.isDeclaracaoSemConflito(),
                a.getEstado().name(), a.getDespacho(), a.getDataDespacho(), a.getMotivo(), a.getDataFimEfectiva());
    }

    public static AcumulacaoFuncoesService.Dados dados(AcumulacaoFuncoesRequestDTO r) {
        return new AcumulacaoFuncoesService.Dados(ChecklistDtos.valor(AcumulacaoFuncoes.Tipo.class, r.getTipo(), "Tipo"),
                ChecklistDtos.valor(AcumulacaoFuncoes.CasoPublico.class, r.getCasoPublico(), "Caso"), Boolean.TRUE.equals(r.getRemunerada()),
                r.getEntidade(), r.getFuncoes(), r.getHorario(), r.getHorasSemanais(), r.getInicio(), r.getFim(),
                Boolean.TRUE.equals(r.getDeclaracaoSemConflito()));
    }
}

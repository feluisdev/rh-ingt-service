package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.MissaoServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.MissaoServicoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.MissaoServicoService;
import cv.igrp.RH_Service.colaboradores.domain.models.MissaoServico;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Da missão de serviço para o DTO, e os termos vindos de fora. */
@Component
@RequiredArgsConstructor
public class MissaoServicoDtos {

    private final MissaoServicoService service;

    public MissaoServicoDTO dto(MissaoServico m, List<String> alertas) {
        return new MissaoServicoDTO(m.getId().getStringValor(),
                new ArrayList<>(m.getParticipantes().stream().map(FuncionarioId::getStringValor).toList()),
                new ArrayList<>(m.getParticipantes().stream().map(service::nome).toList()), m.getDestinoTipo().name(), m.getDestino(),
                m.getObjectivo(), m.getPartida(), m.getRegresso(), m.getTransporte().name(), m.isAlojamentoACargo(), m.isAdiantamento(),
                m.getEstado().name(), m.getPedidoPor() != null ? m.getPedidoPor().getStringValor() : null, m.getDespacho(), m.getMotivo(),
                m.getRelatorio(), m.getDataRelatorio(), m.diasAjudasCusto(), new ArrayList<>(alertas != null ? alertas : List.of()));
    }

    public static MissaoServicoService.Dados dados(MissaoServicoRequestDTO r) {
        return new MissaoServicoService.Dados(ChecklistDtos.valor(MissaoServico.Destino.class, r.getDestinoTipo(), "Tipo de destino"),
                r.getDestino(), r.getObjectivo(), r.getPartida(), r.getRegresso(),
                ChecklistDtos.valor(MissaoServico.Transporte.class, r.getTransporte(), "Transporte"),
                Boolean.TRUE.equals(r.getAlojamentoACargo()), Boolean.TRUE.equals(r.getAdiantamento()));
    }
}

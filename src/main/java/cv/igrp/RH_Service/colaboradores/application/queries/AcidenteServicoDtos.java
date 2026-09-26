package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.AcidenteServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.IncapacidadeAcidenteDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AcidenteServicoService;
import cv.igrp.RH_Service.colaboradores.domain.models.AcidenteServico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Do acidente em serviço para o DTO. */
@Component
@RequiredArgsConstructor
public class AcidenteServicoDtos {

    private final AcidenteServicoService service;

    public AcidenteServicoDTO dto(AcidenteServico a, List<String> alertas) {
        return new AcidenteServicoDTO(a.getId().getStringValor(), a.getFuncionarioId().getStringValor(), service.nome(a.getFuncionarioId()),
                a.getTipo().name(), a.getDataHora(), a.getLocal(), a.getDescricao(), a.getTestemunhas(), a.getDataParticipacao(),
                a.isParticipadoPeloProprio(), a.getEstado().name(), a.getDespacho(), a.getMotivo(), a.getSeguradora(), a.getApolice(),
                a.getParticipacaoSeguradora(),
                new ArrayList<>(a.getIncapacidades().stream().map(i -> new IncapacidadeAcidenteDTO(i.id().toString(), i.tipo().name(), i.inicio(),
                        i.fim())).toList()),
                a.getAlta(), a.getIncapacidadePermanente(), a.isIncapacidadeAbsoluta(),
                new ArrayList<>(alertas != null ? alertas : service.alertas(a)));
    }
}

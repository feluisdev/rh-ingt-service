package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ActoDisciplinarDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.PrazoDisciplinarDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.TramitacaoDisciplinarDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ProcessoDisciplinarService;
import cv.igrp.RH_Service.colaboradores.domain.models.ActoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinar;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Do processo disciplinar (com a tramitação) para o DTO. */
@Component
@RequiredArgsConstructor
public class TramitacaoDisciplinarDtos {

    private final ProcessoDisciplinarService service;

    public TramitacaoDisciplinarDTO dto(ProcessoDisciplinar p, List<String> alertas) {
        LocalDate hoje = LocalDate.now();
        LocalDate execucao = p.getEfeitosAplicadosEm() != null
                ? p.ultimo(ActoDisciplinar.Tipo.EFEITOS).map(ActoDisciplinar::data).orElse(null) : p.dataExecucao();
        return new TramitacaoDisciplinarDTO(p.getId().getStringValor(), p.getFuncionarioId().getStringValor(), service.nome(p.getFuncionarioId()),
                p.getProcessNumber(), nome(p.getEspecie()), nome(p.getFase()), p.getDataInfraccao(), nome(p.getPenaPrevista()), p.prescreveEm(),
                p.getInstrutorId() != null ? p.getInstrutorId().getStringValor() : null, p.getInstrutorNome(), nome(p.getPena()),
                p.getPenaDuracao(), p.getPenalty(), p.getPenaltyStartDate(), p.getPenaltyEndDate(), execucao, p.getEfeitosAplicadosEm() != null,
                p.getNotes(),
                new ArrayList<>(p.getActos().stream().map(a -> new ActoDisciplinarDTO(a.tipo().name(), a.data(), a.dataFim(), a.dias(),
                        nome(a.pena()), a.duracao(), a.texto())).toList()),
                new ArrayList<>(service.prazos(p, hoje).stream().map(x -> new PrazoDisciplinarDTO(x.nome(), x.data(), x.vencido())).toList()),
                new ArrayList<>(alertas != null ? alertas : service.alertas(p)));
    }

    private static String nome(Enum<?> e) {
        return e != null ? e.name() : null;
    }
}

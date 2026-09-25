package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PeriodoProvaDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.PeriodoProvaRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.EntradaServicoDtos;
import cv.igrp.RH_Service.colaboradores.application.queries.PublicacoesDtos;
import cv.igrp.RH_Service.colaboradores.application.services.ProvimentoService;
import cv.igrp.RH_Service.colaboradores.domain.models.PeriodoProva;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PeriodoProvaId;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AccaoPeriodoProvaCommandHandler implements CommandHandler<AccaoPeriodoProvaCommand, ResponseEntity<PeriodoProvaDTO>> {

    private final ProvimentoService service;
    private final FuncionarioRepository funcionarioRepository;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpCommandHandler
    public ResponseEntity<PeriodoProvaDTO> handle(AccaoPeriodoProvaCommand c) {
        var id = PeriodoProvaId.from(Entrada.uuid(c.getPeriodoId(), "o período de prova"));
        PeriodoProvaRequestDTO r = c.getRequest() != null ? c.getRequest() : new PeriodoProvaRequestDTO();
        var avaliacao = PublicacoesDtos.enumOuNulo(PeriodoProva.Avaliacao.class, r.getAvaliacao(), "Avaliação");
        if ("RELATORIO".equals(c.getAccao())) {
            var p = service.registarRelatorio(currentEmployeeResolver.resolve(), id, avaliacao, r.getFundamentacao(), r.getData());
            return ResponseEntity.ok(EntradaServicoDtos.dto(p, funcionarioRepository, List.of()));
        }
        var f = FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador"));
        var res = switch (c.getAccao()) {
            case "CONCLUIR" -> service.concluir(f, id, avaliacao, r.getFundamentacao(), r.getData(), r.getWorkerStateId());
            case "CESSAR" -> service.cessarAntecipadamente(f, id, r.getFundamentacao(), r.getData(), r.getWorkerStateId());
            case "DENUNCIAR" -> service.denunciar(f, id, r.getData(), r.getWorkerStateId());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(EntradaServicoDtos.dto(res.valor(), funcionarioRepository, res.alertas()));
    }
}

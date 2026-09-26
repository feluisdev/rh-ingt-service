package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.MissaoServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.MissaoServicoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.MissaoServicoDtos;
import cv.igrp.RH_Service.colaboradores.application.services.MissaoServicoService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MissaoServicoId;
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
public class AccaoMissaoServicoCommandHandler implements CommandHandler<AccaoMissaoServicoCommand, ResponseEntity<MissaoServicoDTO>> {

    private final MissaoServicoService service;
    private final MissaoServicoDtos dtos;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpCommandHandler
    public ResponseEntity<MissaoServicoDTO> handle(AccaoMissaoServicoCommand c) {
        MissaoServicoRequestDTO r = c.getRequest() != null ? c.getRequest() : new MissaoServicoRequestDTO();
        FuncionarioId eu = c.isComoMe() ? currentEmployeeResolver.resolve() : null;
        if ("PEDIR".equals(c.getAccao())) {
            List<FuncionarioId> participantes = r.getParticipantes() == null ? List.of()
                    : r.getParticipantes().stream().map(p -> FuncionarioId.from(Entrada.uuid(p, "o participante"))).toList();
            var res = eu != null ? service.pedirComo(eu, MissaoServicoDtos.dados(r), participantes)
                    : service.pedir(MissaoServicoDtos.dados(r), participantes);
            return ResponseEntity.status(201).body(dtos.dto(res.missao(), res.alertas()));
        }
        var id = MissaoServicoId.from(Entrada.uuid(c.getMissaoId(), "a missão"));
        var res = switch (c.getAccao()) {
            case "AUTORIZAR" -> eu != null ? service.autorizarComo(eu, id, r.getDespacho()) : service.autorizar(id, r.getDespacho());
            case "RECUSAR" -> eu != null ? service.recusarComo(eu, id, r.getMotivo()) : service.recusar(id, r.getMotivo());
            case "REGRESSO" -> service.realizar(eu, id, r.getRelatorio(), r.getPartida(), r.getRegresso());
            case "CANCELAR" -> service.cancelar(id, r.getMotivo());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(dtos.dto(res.missao(), res.alertas()));
    }
}

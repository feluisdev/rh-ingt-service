package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AcidenteServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.AcidenteServicoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.AcidenteServicoDtos;
import cv.igrp.RH_Service.colaboradores.application.queries.ChecklistDtos;
import cv.igrp.RH_Service.colaboradores.application.services.AcidenteServicoService;
import cv.igrp.RH_Service.colaboradores.domain.models.AcidenteServico;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AcidenteServicoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccaoAcidenteServicoCommandHandler implements CommandHandler<AccaoAcidenteServicoCommand, ResponseEntity<AcidenteServicoDTO>> {

    private final AcidenteServicoService service;
    private final AcidenteServicoDtos dtos;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpCommandHandler
    public ResponseEntity<AcidenteServicoDTO> handle(AccaoAcidenteServicoCommand c) {
        AcidenteServicoRequestDTO r = c.getRequest() != null ? c.getRequest() : new AcidenteServicoRequestDTO();
        FuncionarioId fid = c.isComoMe() ? currentEmployeeResolver.resolve()
                : FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador"));
        if ("PARTICIPAR".equals(c.getAccao())) {
            var res = service.participar(fid, ChecklistDtos.valor(AcidenteServico.Tipo.class, r.getTipo(), "Tipo"), r.getDataHora(), r.getLocal(),
                    r.getDescricao(), r.getTestemunhas(), r.getDataParticipacao(), c.isComoMe());
            return ResponseEntity.status(201).body(dtos.dto(res.acidente(), res.alertas()));
        }
        var id = AcidenteServicoId.from(Entrada.uuid(c.getAcidenteId(), "o acidente"));
        var res = switch (c.getAccao()) {
            case "QUALIFICAR" -> {
                if (r.getEmServico() == null)
                    throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Diga se é, ou não, acidente em serviço.");
                yield service.qualificar(fid, id, r.getEmServico(), r.getDespacho(), r.getMotivo());
            }
            case "INCAPACIDADE" -> service.registarIncapacidade(fid, id,
                    ChecklistDtos.valor(AcidenteServico.TipoIncapacidade.class, r.getTipoIncapacidade(), "Tipo de incapacidade"), r.getInicio(), r.getFim());
            case "ALTA" -> service.darAlta(fid, id, r.getData());
            case "INCAPACIDADE_PERMANENTE" -> service.registarIncapacidadePermanente(fid, id, r.getPercentagem(),
                    Boolean.TRUE.equals(r.getAbsoluta()), Boolean.TRUE.equals(r.getImpedeFuncoes()));
            case "SEGURADORA" -> service.registarSeguradora(fid, id, r.getSeguradora(), r.getApolice(), r.getData());
            case "ENCERRAR" -> service.encerrar(fid, id);
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(dtos.dto(res.acidente(), res.alertas()));
    }
}

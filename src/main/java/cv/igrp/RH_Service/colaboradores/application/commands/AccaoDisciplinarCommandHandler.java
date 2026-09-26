package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AccaoDisciplinarRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.TramitacaoDisciplinarDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.ChecklistDtos;
import cv.igrp.RH_Service.colaboradores.application.queries.TramitacaoDisciplinarDtos;
import cv.igrp.RH_Service.colaboradores.application.services.ProcessoDisciplinarService;
import cv.igrp.RH_Service.colaboradores.domain.models.EspecieProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.PenaDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoDisciplinarId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccaoDisciplinarCommandHandler implements CommandHandler<AccaoDisciplinarCommand, ResponseEntity<TramitacaoDisciplinarDTO>> {

    private final ProcessoDisciplinarService service;
    private final TramitacaoDisciplinarDtos dtos;

    @IgrpCommandHandler
    public ResponseEntity<TramitacaoDisciplinarDTO> handle(AccaoDisciplinarCommand c) {
        var fid = FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador"));
        AccaoDisciplinarRequestDTO r = c.getRequest() != null ? c.getRequest() : new AccaoDisciplinarRequestDTO();
        var pena = ChecklistDtos.valor(PenaDisciplinar.class, r.getPena(), "Pena");
        if ("PARTICIPAR".equals(c.getAccao())) {
            var res = service.participar(fid, r.getNumero(), ChecklistDtos.valor(EspecieProcessoDisciplinar.class, r.getEspecie(), "Espécie de processo"),
                    r.getDataInfraccao(), r.getData(), r.getFactos(), ChecklistDtos.valor(PenaDisciplinar.class, r.getPenaPrevista(), "Pena"));
            return ResponseEntity.status(201).body(dtos.dto(res.processo(), res.alertas()));
        }
        var id = ProcessoDisciplinarId.from(Entrada.uuid(c.getProcessoId(), "o processo"));
        var instrutor = Entrada.uuidOpcional(r.getInstrutorId(), "o instrutor");
        var instrutorId = instrutor != null ? FuncionarioId.from(instrutor) : null;
        var res = switch (c.getAccao()) {
            case "INSTAURAR" -> service.instaurar(fid, id, r.getDespacho(), r.getData(), r.getEntidade(), instrutorId, r.getInstrutorNome());
            case "NOMEAR_INSTRUTOR" -> service.nomearInstrutor(fid, id, instrutorId, r.getInstrutorNome(), r.getData());
            case "INICIAR_INSTRUCAO" -> service.iniciarInstrucao(fid, id, r.getData());
            case "PRORROGAR_INSTRUCAO" -> service.prorrogarInstrucao(fid, id, r.getDias(), r.getData());
            case "SUSPENDER" -> service.suspenderPreventivamente(fid, id, r.getData(), r.getDias(), Boolean.TRUE.equals(r.getPerdaVencimento()));
            case "LEVANTAR_SUSPENSAO" -> service.levantarSuspensao(fid, id, r.getData());
            case "ACUSAR" -> service.acusar(fid, id, r.getData(), pena, r.getTexto());
            case "NOTIFICAR_ACUSACAO" -> service.notificarAcusacao(fid, id, r.getData(), r.getDias(), Boolean.TRUE.equals(r.getComplexo()));
            case "DEFESA" -> service.registarDefesa(fid, id, r.getData(), r.getTexto());
            case "RELATORIO" -> service.relatorio(fid, id, r.getData(), pena, r.getDuracao(), r.getTexto());
            case "DECIDIR" -> service.decidir(fid, id, r.getData(), pena, r.getDuracao(), r.getEntidade(), r.getTexto());
            case "NOTIFICAR_DECISAO" -> service.notificarDecisao(fid, id, r.getData());
            case "RECURSO" -> service.interporRecurso(fid, id, r.getData(), r.getTexto());
            case "DECIDIR_RECURSO" -> service.decidirRecurso(fid, id, r.getData(),
                    ChecklistDtos.valor(ProcessoDisciplinar.ResultadoRecurso.class, r.getResultado(), "Resultado do recurso"), pena, r.getDuracao());
            case "ARQUIVAR" -> service.arquivar(fid, id, r.getData(), r.getMotivo());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(dtos.dto(res.processo(), res.alertas()));
    }
}

package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ProvimentoDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.EntradaServicoDtos;
import cv.igrp.RH_Service.colaboradores.application.services.ProvimentoService;
import cv.igrp.RH_Service.colaboradores.domain.models.ModalidadeProvimento;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RegistarProvimentoCommandHandler implements CommandHandler<RegistarProvimentoCommand, ResponseEntity<ProvimentoDTO>> {

    private final ProvimentoService service;

    @IgrpCommandHandler
    public ResponseEntity<ProvimentoDTO> handle(RegistarProvimentoCommand c) {
        var r = Entrada.corpo(c.getRequest(), "a forma do provimento e a data da posse");
        var f = FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador"));
        UUID tutor = Entrada.uuidOpcional(r.getTutorId(), "o tutor");
        var res = service.registar(f, ModalidadeProvimento.de(r.getModalidade()), r.getDespachoNumero(), r.getDespachoData(),
                r.getDataPosse(), r.getConcursoRef(), Boolean.TRUE.equals(r.getVemDeOutraCarreira()),
                tutor != null ? FuncionarioId.from(tutor) : null, r.getMesesPrevistos(), r.getObservacoes());
        return ResponseEntity.status(201).body(EntradaServicoDtos.dto(res.valor(), res.alertas()));
    }
}

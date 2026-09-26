package cv.igrp.RH_Service.recrutamento.application.commands;

import cv.igrp.RH_Service.recrutamento.application.dto.ConcursoDTO;
import cv.igrp.RH_Service.recrutamento.application.queries.ConcursosDtos;
import cv.igrp.RH_Service.recrutamento.application.services.ConcursoService;
import cv.igrp.RH_Service.recrutamento.domain.valueobject.ConcursoId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GuardarConcursoCommandHandler implements CommandHandler<GuardarConcursoCommand, ResponseEntity<ConcursoDTO>> {

    private final ConcursoService service;

    @IgrpCommandHandler
    public ResponseEntity<ConcursoDTO> handle(GuardarConcursoCommand c) {
        var dados = ConcursosDtos.dados(Entrada.corpo(c.getRequest(), "a referência, a finalidade, a modalidade e a categoria"));
        if (c.getConcursoId() == null)
            return ResponseEntity.status(201).body(ConcursosDtos.dto(service.criar(dados), null));
        var id = ConcursoId.from(Entrada.uuid(c.getConcursoId(), "o concurso"));
        var concurso = service.actualizar(id, dados);
        return ResponseEntity.ok(ConcursosDtos.dto(concurso, service.candidaturas(id)));
    }
}

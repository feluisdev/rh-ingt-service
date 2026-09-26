package cv.igrp.RH_Service.recrutamento.application.commands;

import cv.igrp.RH_Service.recrutamento.application.dto.ConcursoDTO;
import cv.igrp.RH_Service.recrutamento.application.dto.ConcursoRequestDTO;
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
public class AccaoConcursoCommandHandler implements CommandHandler<AccaoConcursoCommand, ResponseEntity<ConcursoDTO>> {

    private final ConcursoService service;

    @IgrpCommandHandler
    public ResponseEntity<ConcursoDTO> handle(AccaoConcursoCommand c) {
        var id = ConcursoId.from(Entrada.uuid(c.getConcursoId(), "o concurso"));
        ConcursoRequestDTO r = c.getRequest() != null ? c.getRequest() : new ConcursoRequestDTO();
        var concurso = switch (c.getAccao()) {
            case "ABRIR" -> service.abrir(id);
            case "ENCERRAR" -> service.encerrarCandidaturas(id);
            case "AVALIAR" -> service.iniciarAvaliacao(id);
            case "LISTA_PROVISORIA" -> service.listaProvisoria(id);
            case "HOMOLOGAR" -> service.homologar(id, r.getDespacho(), r.getData());
            case "CONCLUIR" -> service.concluir(id);
            case "ANULAR" -> service.anular(id, r.getMotivo());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(ConcursosDtos.dto(concurso, service.candidaturas(id)));
    }
}

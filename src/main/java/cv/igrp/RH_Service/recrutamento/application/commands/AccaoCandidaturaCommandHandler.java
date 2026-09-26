package cv.igrp.RH_Service.recrutamento.application.commands;

import cv.igrp.RH_Service.recrutamento.application.dto.CandidaturaDTO;
import cv.igrp.RH_Service.recrutamento.application.dto.CandidaturaRequestDTO;
import cv.igrp.RH_Service.recrutamento.application.queries.ConcursosDtos;
import cv.igrp.RH_Service.recrutamento.application.services.ConcursoService;
import cv.igrp.RH_Service.recrutamento.domain.models.MetodoSelecao;
import cv.igrp.RH_Service.recrutamento.domain.valueobject.CandidaturaId;
import cv.igrp.RH_Service.recrutamento.domain.valueobject.ConcursoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccaoCandidaturaCommandHandler implements CommandHandler<AccaoCandidaturaCommand, ResponseEntity<CandidaturaDTO>> {

    private final ConcursoService service;

    @IgrpCommandHandler
    public ResponseEntity<CandidaturaDTO> handle(AccaoCandidaturaCommand c) {
        var id = ConcursoId.from(Entrada.uuid(c.getConcursoId(), "o concurso"));
        CandidaturaRequestDTO r = c.getRequest() != null ? c.getRequest() : new CandidaturaRequestDTO();
        if ("CANDIDATAR".equals(c.getAccao())) {
            var x = service.candidatar(id, new ConcursoService.Candidato(r.getNome(), r.getDocumento(), r.getNif(), r.getEmail(),
                    r.getTelefone(), r.getHabilitacao(), Boolean.TRUE.equals(r.getDeficiencia()),
                    Entrada.uuidOpcional(r.getFuncionarioId(), "o colaborador"), Boolean.TRUE.equals(r.getVinculadoAdministracao())));
            return ResponseEntity.status(201).body(ConcursosDtos.dto(x));
        }
        var cid = CandidaturaId.from(Entrada.uuid(c.getCandidaturaId(), "a candidatura"));
        var x = switch (c.getAccao()) {
            case "ADMITIR" -> service.admitir(id, cid);
            case "EXCLUIR" -> service.proporExclusao(id, cid, r.getMotivo());
            case "AUDIENCIA" -> {
                if (r.getExcluir() == null)
                    throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Diga se exclui de vez ou admite o candidato.");
                yield service.decidirAudiencia(id, cid, r.getExcluir(), r.getResposta());
            }
            case "NOTA" -> service.registarNota(id, cid, ConcursosDtos.valor(MetodoSelecao.class, r.getMetodo(), "Método de selecção"), r.getNota());
            case "PROVER" -> service.prover(id, cid, Entrada.uuidOpcional(r.getLugarId(), "o Lugar"));
            case "DESISTIR" -> service.desistir(id, cid);
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(ConcursosDtos.dto(x));
    }
}

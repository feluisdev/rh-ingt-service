package cv.igrp.RH_Service.formacao.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.formacao.application.dto.AccaoFormacaoDTO;
import cv.igrp.RH_Service.formacao.application.dto.AccaoFormacaoRequestDTO;
import cv.igrp.RH_Service.formacao.application.queries.FormacaoDtos;
import cv.igrp.RH_Service.formacao.application.services.FormacaoService;
import cv.igrp.RH_Service.formacao.domain.models.AccaoFormacao;
import cv.igrp.RH_Service.formacao.domain.valueobject.AccaoFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.InscricaoFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.NecessidadeFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.PlanoFormacaoId;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccaoFormacaoCommandHandler implements CommandHandler<AccaoFormacaoCommand, ResponseEntity<AccaoFormacaoDTO>> {

    private final FormacaoService service;
    private final FormacaoDtos dtos;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpCommandHandler
    public ResponseEntity<AccaoFormacaoDTO> handle(AccaoFormacaoCommand c) {
        AccaoFormacaoRequestDTO r = c.getRequest() != null ? c.getRequest() : new AccaoFormacaoRequestDTO();
        if ("PLANEAR".equals(c.getAccao())) {
            var plano = Entrada.uuidOpcional(r.getPlanoId(), "o plano");
            var a = service.planear(FormacaoDtos.dados(r), plano != null ? PlanoFormacaoId.from(plano) : null,
                    r.getNecessidades() == null ? null : r.getNecessidades().stream().map(n -> NecessidadeFormacaoId.from(Entrada.uuid(n, "a necessidade"))).toList());
            return ResponseEntity.status(201).body(dtos.dto(a, null));
        }
        var id = AccaoFormacaoId.from(Entrada.uuid(c.getAccaoId(), "a acção"));
        var fid = Entrada.uuidOpcional(r.getFuncionarioId(), "o colaborador");
        var para = fid != null ? FuncionarioId.from(fid) : null;
        FuncionarioId eu = c.isComoMe() ? currentEmployeeResolver.resolve() : null;
        InscricaoFormacaoId iid = c.getInscricaoId() != null ? InscricaoFormacaoId.from(Entrada.uuid(c.getInscricaoId(), "a inscrição")) : null;
        var a = switch (c.getAccao()) {
            case "ACTUALIZAR" -> service.actualizar(id, FormacaoDtos.dados(r));
            case "ABRIR" -> service.abrirInscricoes(id);
            case "INICIAR" -> service.iniciar(id);
            case "CONCLUIR" -> service.concluir(id);
            case "CANCELAR" -> service.cancelar(id, r.getMotivo());
            case "INSCREVER" -> eu != null ? service.inscreverComo(eu, id, para)
                    : service.inscrever(id, para != null ? para : FuncionarioId.from(Entrada.uuid(r.getFuncionarioId(), "o colaborador")));
            case "ADMITIR" -> eu != null ? service.decidirComo(eu, id, iid, true, r.getMotivo()) : service.decidir(id, iid, true, r.getMotivo());
            case "RECUSAR" -> eu != null ? service.decidirComo(eu, id, iid, false, r.getMotivo()) : service.decidir(id, iid, false, r.getMotivo());
            case "DESISTIR" -> eu != null ? service.desistirComo(eu, id, r.getMotivo()) : service.desistir(id, iid, r.getMotivo());
            case "AVALIAR" -> service.avaliar(id, iid, FormacaoDtos.valor(AccaoFormacao.EstadoInscricao.class, r.getResultado(), "Resultado"),
                    r.getDiasPresenca());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(dtos.dto(a, eu != null && ("DESISTIR".equals(c.getAccao()) || para == null) ? eu : null));
    }
}

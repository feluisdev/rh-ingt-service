package cv.igrp.RH_Service.formacao.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.formacao.application.dto.PlanoFormacaoDTO;
import cv.igrp.RH_Service.formacao.application.dto.PlanoFormacaoRequestDTO;
import cv.igrp.RH_Service.formacao.application.queries.FormacaoDtos;
import cv.igrp.RH_Service.formacao.application.services.FormacaoService;
import cv.igrp.RH_Service.formacao.domain.models.PlanoFormacao;
import cv.igrp.RH_Service.formacao.domain.valueobject.PlanoFormacaoId;
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
public class AccaoPlanoFormacaoCommandHandler implements CommandHandler<AccaoPlanoFormacaoCommand, ResponseEntity<PlanoFormacaoDTO>> {

    private final FormacaoService service;
    private final FormacaoDtos dtos;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpCommandHandler
    public ResponseEntity<PlanoFormacaoDTO> handle(AccaoPlanoFormacaoCommand c) {
        PlanoFormacaoRequestDTO r = c.getRequest() != null ? c.getRequest() : new PlanoFormacaoRequestDTO();
        if ("CRIAR".equals(c.getAccao())) {
            if (r.getAno() == null) throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Indique o ano do plano.");
            return ResponseEntity.status(201).body(dtos.dto(service.criarPlano(r.getAno(), Entrada.uuidOpcional(r.getUnidadeId(), "a unidade"),
                    r.getDesignacao())));
        }
        var id = PlanoFormacaoId.from(Entrada.uuid(c.getPlanoId(), "o plano"));
        var fid = Entrada.uuidOpcional(r.getFuncionarioId(), "o colaborador");
        var para = fid != null ? FuncionarioId.from(fid) : null;
        var prioridade = FormacaoDtos.valor(PlanoFormacao.Prioridade.class, r.getPrioridade(), "Prioridade");
        var p = switch (c.getAccao()) {
            case "NECESSIDADE" -> c.isComoMe()
                    ? service.identificarComo(currentEmployeeResolver.resolve(), id, r.getTema(), para, prioridade, r.getJustificacao())
                    : service.identificar(id, r.getTema(), para, prioridade, r.getJustificacao());
            case "APROVAR" -> service.aprovarPlano(id, r.getDespacho(), r.getData());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(dtos.dto(p));
    }
}

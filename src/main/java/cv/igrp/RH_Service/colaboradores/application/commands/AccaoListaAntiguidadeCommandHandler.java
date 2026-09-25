package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ListaAntiguidadeOficialDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ListaAntiguidadeRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ReclamacaoAntiguidadeDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ReclamacaoAntiguidadeRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.ListasAntiguidadeDtos;
import cv.igrp.RH_Service.colaboradores.application.services.CicloListaAntiguidadeService;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.ReclamacaoAntiguidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ListaAntiguidadeOficialId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ReclamacaoAntiguidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AccaoListaAntiguidadeCommandHandler implements CommandHandler<AccaoListaAntiguidadeCommand, ResponseEntity<ListaAntiguidadeOficialDTO>> {
    private final CicloListaAntiguidadeService service;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpCommandHandler
    public ResponseEntity<ListaAntiguidadeOficialDTO> handle(AccaoListaAntiguidadeCommand c) {
        var id = ListaAntiguidadeOficialId.from(Entrada.uuid(c.getListaId(), "a lista de antiguidade"));
        ListaAntiguidadeRequestDTO r = c.getRequest() != null ? c.getRequest() : new ListaAntiguidadeRequestDTO();
        List<String> alertas = List.of();
        var l = switch (c.getAccao()) {
            case "AFIXAR" -> service.afixar(id, r.getData(), r.getLocal());
            case "RECALCULAR" -> service.recalcular(id);
            case "DEFINITIVA" -> service.tornarDefinitiva(id);
            case "PUBLICAR" -> {
                var res = service.publicar(id, r.getSerie(), r.getNumero(), r.getData());
                alertas = res.alertas();
                yield res.valor();
            }
            case "ANULAR" -> service.anular(id, r.getMotivo());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(ListasAntiguidadeDtos.dto(l, service.reclamacoes(id), true, null,
                f -> Nomes.de(funcionarioRepository, f), alertas));
    }
}

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
public class AccaoReclamacaoAntiguidadeCommandHandler implements CommandHandler<AccaoReclamacaoAntiguidadeCommand, ResponseEntity<ReclamacaoAntiguidadeDTO>> {
    private final CicloListaAntiguidadeService service;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpCommandHandler
    public ResponseEntity<ReclamacaoAntiguidadeDTO> handle(AccaoReclamacaoAntiguidadeCommand c) {
        var lista = ListaAntiguidadeOficialId.from(Entrada.uuid(c.getListaId(), "a lista de antiguidade"));
        var id = ReclamacaoAntiguidadeId.from(Entrada.uuid(c.getReclamacaoId(), "a reclamação"));
        ReclamacaoAntiguidadeRequestDTO r = c.getRequest() != null ? c.getRequest() : new ReclamacaoAntiguidadeRequestDTO();
        List<String> alertas = List.of();
        ReclamacaoAntiguidade rec;
        switch (c.getAccao()) {
            case "DECIDIR" -> {
                if (r.getDeferida() == null)
                    throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Diga se a reclamação é deferida ou indeferida.");
                var res = service.decidir(lista, id, r.getDeferida(), r.getDecisao());
                rec = res.valor();
                alertas = res.alertas();
            }
            case "RECORRER" -> rec = service.recorrer(lista, id, r.getTexto());
            case "DECIDIR_RECURSO" -> {
                if (r.getProvido() == null)
                    throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Diga se o recurso é provido ou não.");
                rec = service.decidirRecurso(lista, id, r.getProvido(), r.getDecisao());
            }
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        }
        return ResponseEntity.ok(ListasAntiguidadeDtos.dto(rec, x -> Nomes.de(funcionarioRepository, x), alertas));
    }
}

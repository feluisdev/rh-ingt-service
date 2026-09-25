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
public class ReclamarListaAntiguidadeCommandHandler implements CommandHandler<ReclamarListaAntiguidadeCommand, ResponseEntity<ReclamacaoAntiguidadeDTO>> {
    private final CicloListaAntiguidadeService service;
    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpCommandHandler
    public ResponseEntity<ReclamacaoAntiguidadeDTO> handle(ReclamarListaAntiguidadeCommand c) {
        ReclamacaoAntiguidadeRequestDTO r = Entrada.corpo(c.getRequest(), "o fundamento e o texto da reclamação");
        var lista = ListaAntiguidadeOficialId.from(Entrada.uuid(c.getListaId(), "a lista de antiguidade"));
        FuncionarioId f = c.isPeloProprio() ? currentEmployeeResolver.resolve()
                : FuncionarioId.from(Entrada.uuid(r.getFuncionarioId(), "o colaborador"));
        var rec = service.reclamar(lista, f, fundamento(r.getFundamento()), r.getTexto(),
                Boolean.TRUE.equals(r.getNoEstrangeiro()), c.isPeloProprio());
        return ResponseEntity.status(201).body(ListasAntiguidadeDtos.dto(rec, x -> Nomes.de(funcionarioRepository, x), List.of()));
    }

    private static ReclamacaoAntiguidade.Fundamento fundamento(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return ReclamacaoAntiguidade.Fundamento.valueOf(v.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Fundamento desconhecido. Use omissão, graduação, situação na lista ou contagem do tempo.");
        }
    }
}

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
public class AprovarListaAntiguidadeCommandHandler implements CommandHandler<AprovarListaAntiguidadeCommand, ResponseEntity<ListaAntiguidadeOficialDTO>> {
    private final CicloListaAntiguidadeService service;

    @IgrpCommandHandler
    public ResponseEntity<ListaAntiguidadeOficialDTO> handle(AprovarListaAntiguidadeCommand c) {
        ListaAntiguidadeRequestDTO r = Entrada.corpo(c.getRequest(), "o ano, o serviço e quem aprova");
        var l = service.aprovar(r.getAno(), Entrada.uuid(r.getUnidadeId(), "a unidade orgânica"),
                !Boolean.FALSE.equals(r.getIncluirSubunidades()), r.getAprovadaPor(), r.getDataAprovacao());
        return ResponseEntity.status(201).body(ListasAntiguidadeDtos.dto(l, List.of(), true, null, null, List.of()));
    }
}

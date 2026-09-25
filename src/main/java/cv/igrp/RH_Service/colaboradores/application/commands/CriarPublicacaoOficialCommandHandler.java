package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PublicacaoOficialDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.PublicacoesDtos;
import cv.igrp.RH_Service.colaboradores.application.services.PublicacoesService;
import cv.igrp.RH_Service.colaboradores.domain.models.PublicacaoOficial;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
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
public class CriarPublicacaoOficialCommandHandler implements CommandHandler<CriarPublicacaoOficialCommand, ResponseEntity<PublicacaoOficialDTO>> {

    private final PublicacoesService service;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpCommandHandler
    public ResponseEntity<PublicacaoOficialDTO> handle(CriarPublicacaoOficialCommand c) {
        var r = Entrada.corpo(c.getRequest(), "o tipo do acto e o sumário");
        UUID f = Entrada.uuidOpcional(r.getFuncionarioId(), "o colaborador");
        var p = service.aPublicar(PublicacoesDtos.enumOuNulo(PublicacaoOficial.TipoActo.class, r.getTipoActo(), "Tipo de acto"),
                PublicacoesDtos.enumOuNulo(PublicacaoOficial.Meio.class, r.getMeio(), "Meio de publicação"),
                f != null ? FuncionarioId.from(f) : null, "MANUAL", UUID.randomUUID().toString(), r.getSumario(), r.getDataActo())
                .orElseThrow();
        return ResponseEntity.status(201).body(PublicacoesDtos.dto(p, funcionarioRepository));
    }
}

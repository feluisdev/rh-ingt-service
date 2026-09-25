package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PublicacaoOficialDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.PublicacaoOficialRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.PublicacoesDtos;
import cv.igrp.RH_Service.colaboradores.application.services.PublicacoesService;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PublicacaoOficialId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccaoPublicacaoOficialCommandHandler implements CommandHandler<AccaoPublicacaoOficialCommand, ResponseEntity<PublicacaoOficialDTO>> {

    private final PublicacoesService service;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpCommandHandler
    public ResponseEntity<PublicacaoOficialDTO> handle(AccaoPublicacaoOficialCommand c) {
        var id = PublicacaoOficialId.from(Entrada.uuid(c.getPublicacaoId(), "a publicação"));
        PublicacaoOficialRequestDTO r = c.getRequest() != null ? c.getRequest() : new PublicacaoOficialRequestDTO();
        var p = switch (c.getAccao()) {
            case "EXTRACTO" -> service.gerarExtracto(id);
            case "PUBLICADA" -> service.registarPublicacao(id, r.getSerie(), r.getNumero(), r.getDataPublicacao());
            case "CANCELAR" -> service.cancelar(id, r.getMotivo());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(PublicacoesDtos.dto(p, funcionarioRepository));
    }
}

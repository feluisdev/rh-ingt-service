package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoDeclaracaoDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.DocumentosDtos;
import cv.igrp.RH_Service.colaboradores.application.services.DeclaracoesService;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoDeclaracaoId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DecidirDeclaracaoCommandHandler implements CommandHandler<DecidirDeclaracaoCommand, ResponseEntity<PedidoDeclaracaoDTO>> {

    private final DeclaracoesService declaracoesService;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpCommandHandler
    public ResponseEntity<PedidoDeclaracaoDTO> handle(DecidirDeclaracaoCommand command) {
        var f = FuncionarioId.from(Entrada.uuid(command.getFuncionarioId(), "o colaborador"));
        var id = PedidoDeclaracaoId.from(Entrada.uuid(command.getPedidoId(), "o pedido de declaração"));
        var res = command.isEmitir() ? declaracoesService.emitir(f, id)
                : declaracoesService.recusar(f, id, command.getRequest() != null ? command.getRequest().getMotivo() : null);
        return ResponseEntity.ok(DocumentosDtos.dto(res, funcionarioRepository.findById(f).orElse(null)));
    }
}

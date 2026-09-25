package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoDeclaracaoDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.DocumentosDtos;
import cv.igrp.RH_Service.colaboradores.application.services.DeclaracoesService;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoDeclaracao;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PedirDeclaracaoCommandHandler implements CommandHandler<PedirDeclaracaoCommand, ResponseEntity<PedidoDeclaracaoDTO>> {

    private final DeclaracoesService declaracoesService;
    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpCommandHandler
    public ResponseEntity<PedidoDeclaracaoDTO> handle(PedirDeclaracaoCommand command) {
        var r = Entrada.corpo(command.getRequest(), "o tipo de declaração");
        FuncionarioId f = command.isPeloProprio() ? currentEmployeeResolver.resolve()
                : FuncionarioId.from(Entrada.uuid(command.getFuncionarioId(), "o colaborador"));
        var res = declaracoesService.pedir(f, PedidoDeclaracao.Tipo.de(r.getTipo()), r.getFinalidade(), command.isPeloProprio());
        return ResponseEntity.status(201).body(DocumentosDtos.dto(res, funcionarioRepository.findById(f).orElse(null)));
    }
}

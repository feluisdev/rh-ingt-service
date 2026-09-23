package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.TrabalhoSuplementarService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TrabalhoSuplementarId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Autorizar ou recusar um pedido. Pela chefia ({@code /me/equipa}): tem de ser a chefia directa. Pelo RH: sempre. */
@Component
@RequiredArgsConstructor
public class DecidirTrabalhoSuplementarCommandHandler
        implements CommandHandler<DecidirTrabalhoSuplementarCommand, ResponseEntity<SuccessResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final TrabalhoSuplementarService trabalhoSuplementarService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(DecidirTrabalhoSuplementarCommand command) {
        FuncionarioId chefe = command.isPelaChefia() ? currentEmployeeResolver.resolve() : null;
        FuncionarioId funcionario = command.getFuncionarioId() != null ? FuncionarioId.from(command.getFuncionarioId()) : null;
        String motivo = command.getRequest() != null ? command.getRequest().getMotivo() : null;
        var t = trabalhoSuplementarService.decidir(chefe, funcionario, TrabalhoSuplementarId.from(command.getTrabalhoId()),
                command.isAutorizar(), motivo);
        return ResponseEntity.ok(SuccessResponseDTO.de(t.getId().getStringValor()));
    }
}

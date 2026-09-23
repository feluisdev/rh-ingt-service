package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.AssiduidadeService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MarcacaoAssiduidadeId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Validar ou rejeitar um pedido de correcção. Pela chefia ({@code /me/equipa}): quem decide é o
 * utilizador autenticado, e tem de ser a chefia directa. Pelo RH: decide sempre.
 */
@Component
@RequiredArgsConstructor
public class DecidirMarcacaoCommandHandler implements CommandHandler<DecidirMarcacaoCommand, ResponseEntity<SuccessResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final AssiduidadeService assiduidadeService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(DecidirMarcacaoCommand command) {
        FuncionarioId chefe = command.isPelaChefia() ? currentEmployeeResolver.resolve() : null;
        FuncionarioId funcionario = command.getFuncionarioId() != null ? FuncionarioId.from(command.getFuncionarioId()) : null;
        String motivo = command.getRequest() != null ? command.getRequest().getMotivo() : null;
        var m = assiduidadeService.decidir(chefe, funcionario, MarcacaoAssiduidadeId.from(command.getMarcacaoId()),
                command.isValidar(), motivo);
        return ResponseEntity.ok(SuccessResponseDTO.de(m.getId().getStringValor()));
    }
}

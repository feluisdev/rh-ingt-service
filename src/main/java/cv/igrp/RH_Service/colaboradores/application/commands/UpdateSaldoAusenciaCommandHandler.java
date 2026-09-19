package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SaldoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component("colabsUpdateSaldoAusenciaCommandHandler")
@RequiredArgsConstructor
public class UpdateSaldoAusenciaCommandHandler
        implements CommandHandler<UpdateSaldoAusenciaCommand, ResponseEntity<SuccessResponseDTO>> {

    private final SaldoAusenciaRepository saldoRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(UpdateSaldoAusenciaCommand command) {
        if (command.getDiasDireito() == null || command.getDiasDireito() < 0)
            throw IgrpResponseStatusException.badRequest("diasDireito deve ser >= 0");

        var saldo = saldoRepository.findById(SaldoAusenciaId.from(command.getSaldoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Saldo não encontrado: " + command.getSaldoId()));

        saldo.atualizarDiasDireito(command.getDiasDireito());
        saldoRepository.save(saldo);
        return ResponseEntity.ok(SuccessResponseDTO.de(command.getSaldoId()));
    }
}

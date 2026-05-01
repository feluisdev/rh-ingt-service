package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SaldoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsUpdateSaldoAusenciaCommandHandler")
@RequiredArgsConstructor
public class UpdateSaldoAusenciaCommandHandler
        implements CommandHandler<UpdateSaldoAusenciaCommand, ResponseEntity<Map<String, ?>>> {

    private final SaldoAusenciaRepository saldoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(UpdateSaldoAusenciaCommand command) {
        if (command.getDiasDireito() == null || command.getDiasDireito() < 0)
            throw IgrpResponseStatusException.badRequest("diasDireito deve ser >= 0");

        var saldo = saldoRepository.findById(SaldoAusenciaId.from(command.getSaldoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Saldo não encontrado: " + command.getSaldoId()));

        saldo.atualizarDiasDireito(command.getDiasDireito());
        saldoRepository.save(saldo);
        return ResponseEntity.ok(Map.of("message", "Saldo actualizado com sucesso"));
    }
}

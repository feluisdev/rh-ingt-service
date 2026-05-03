package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsRejeitarLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class RejeitarLicencaMobilidadeCommandHandler
        implements CommandHandler<RejeitarLicencaMobilidadeCommand, ResponseEntity<Map<String, ?>>> {

    private final LicencaMobilidadeRepository licencaRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(RejeitarLicencaMobilidadeCommand command) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));

        if (!licenca.isPending())
            throw IgrpResponseStatusException.of(HttpStatus.CONFLICT,
                    "Apenas registos PENDING podem ser rejeitados. Estado actual: " + licenca.getStatus());

        if (command.getRejectionReason() == null || command.getRejectionReason().isBlank())
            throw IgrpResponseStatusException.badRequest("O motivo de rejeição é obrigatório.");

        licenca.rejeitar(command.getRejectionReason());
        licencaRepository.save(licenca);

        return ResponseEntity.ok(Map.of("message", "Rejeitado com sucesso"));
    }
}

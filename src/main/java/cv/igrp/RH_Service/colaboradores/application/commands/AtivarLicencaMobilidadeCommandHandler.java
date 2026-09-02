package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

/**
 * Alias de /approve (mantido por compatibilidade): activa a licença/mobilidade e,
 * sendo MOBILIDADE, abre a afectação no Lugar de destino via AssignmentService
 * (mesmo caminho do AprovarLicencaMobilidadeCommandHandler — modelo Position).
 */
@Component("colabsAtivarLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class AtivarLicencaMobilidadeCommandHandler
        implements CommandHandler<AtivarLicencaMobilidadeCommand, ResponseEntity<Map<String, ?>>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final SubtipoLicencaMobilidadeRepository subtipoRepository;
    private final AssignmentService assignmentService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(AtivarLicencaMobilidadeCommand command) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));
        if (Boolean.TRUE.equals(licenca.getIsActive()))
            return ResponseEntity.ok(Map.of("message", "Já se encontra activo"));

        licenca.ativar();
        licencaRepository.save(licenca);

        var subtipo = subtipoRepository.findById(
                SubtipoLicencaMobilidadeId.from(licenca.getSubtipoId().getValor())).orElse(null);
        boolean isMobilidade = subtipo != null && "MOBILIDADE".equals(subtipo.getRecordType());

        if (isMobilidade) {
            if (licenca.getDestinationPositionId() == null)
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "A mobilidade exige um Lugar de destino (destinationPositionId) para abrir a afectação.");
            String notes = "Mobilidade" + (licenca.getDespachoNumero() != null
                    ? " (despacho " + licenca.getDespachoNumero() + ")" : "");
            assignmentService.afectarMobilidade(licenca.getFuncionarioId(),
                    licenca.getDestinationPositionId(), LocalDate.now(), notes);
        }

        return ResponseEntity.ok(Map.of("message", "Activado com sucesso"));
    }
}

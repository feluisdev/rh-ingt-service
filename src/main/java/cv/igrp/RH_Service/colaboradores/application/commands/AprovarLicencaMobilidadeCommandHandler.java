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

@Component("colabsAprovarLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class AprovarLicencaMobilidadeCommandHandler
        implements CommandHandler<AprovarLicencaMobilidadeCommand, ResponseEntity<Map<String, ?>>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final AssignmentService assignmentService;
    private final SubtipoLicencaMobilidadeRepository subtipoRepository;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(AprovarLicencaMobilidadeCommand command) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));

        if (!licenca.isPending())
            throw IgrpResponseStatusException.of(HttpStatus.CONFLICT,
                    "Apenas registos PENDING podem ser aprovados. Estado actual: " + licenca.getStatus());

        licenca.aprovar();
        licencaRepository.save(licenca);

        var subtipo = subtipoRepository.findById(SubtipoLicencaMobilidadeId.from(licenca.getSubtipoId().getValor()))
                .orElse(null);
        boolean isMobilidade = subtipo != null && "MOBILIDADE".equals(subtipo.getRecordType());

        if (isMobilidade) {
            if (licenca.getDestinationPositionId() == null)
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "A mobilidade exige um Lugar de destino (destinationPositionId) para abrir a afectação.");

            var hoje = LocalDate.now();
            String notes = "Mobilidade" + (licenca.getDespachoNumero() != null
                    ? " (despacho " + licenca.getDespachoNumero() + ")" : "");
            assignmentService.afectarMobilidade(licenca.getFuncionarioId(),
                    licenca.getDestinationPositionId(), hoje, notes);
        }

        return ResponseEntity.ok(Map.of("message", "Aprovado com sucesso"));
    }
}

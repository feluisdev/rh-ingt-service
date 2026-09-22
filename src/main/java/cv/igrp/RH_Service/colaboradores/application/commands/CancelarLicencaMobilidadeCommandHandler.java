package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * Cancelar a licença ou a mobilidade (antes de começar, ou já em vigor). Como a mobilidade
 * transitória não toca na afectação, cancelar é fechar o registo — não há nada a reverter.
 */
@Component
@RequiredArgsConstructor
public class CancelarLicencaMobilidadeCommandHandler
        implements CommandHandler<CancelarLicencaMobilidadeCommand, ResponseEntity<SuccessResponseDTO>> {

    private final LicencaMobilidadeRepository licencaRepository;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<SuccessResponseDTO> handle(CancelarLicencaMobilidadeCommand command) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));

        licenca.cancelar(java.time.LocalDate.now());
        licencaRepository.save(licenca);

        return ResponseEntity.ok(SuccessResponseDTO.de(command.getLicencaId()));
    }
}

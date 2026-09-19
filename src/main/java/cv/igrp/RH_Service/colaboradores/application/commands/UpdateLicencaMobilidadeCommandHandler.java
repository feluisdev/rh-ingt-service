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


@Component("colabsUpdateLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class UpdateLicencaMobilidadeCommandHandler
        implements CommandHandler<UpdateLicencaMobilidadeCommand, ResponseEntity<SuccessResponseDTO>> {

    private final LicencaMobilidadeRepository licencaRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(UpdateLicencaMobilidadeCommand command) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Licença/mobilidade não encontrada: " + command.getLicencaId()));

        var dto = command.getRequest();
        if (dto.getDataFim() != null && dto.getDataFim().isBefore(dto.getDataInicio()))
            throw IgrpResponseStatusException.badRequest("dataFim não pode ser anterior a dataInicio.");

        licenca.atualizar(dto.getDataInicio(), dto.getDataFim(),
                dto.getEntidadeDestino(), dto.getDespachoNumero(), dto.getObservacoes());
        licencaRepository.save(licenca);
        return ResponseEntity.ok(SuccessResponseDTO.de(command.getLicencaId()));
    }
}

package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.VinculoLaboralResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.VinculoLaboral;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.VinculoLaboralRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.VinculoLaboralMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.VinculoLaboralId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateVinculoLaboralCommandHandler implements CommandHandler<UpdateVinculoLaboralCommand, ResponseEntity<VinculoLaboralResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateVinculoLaboralCommandHandler.class);

    private final VinculoLaboralRepository vinculoLaboralRepository;
    private final VinculoLaboralMapper vinculoLaboralMapper;

    @IgrpCommandHandler
    public ResponseEntity<VinculoLaboralResponseDTO> handle(UpdateVinculoLaboralCommand command) {
        var id = VinculoLaboralId.from(UUID.fromString(command.getVinculoLaboralId()));

        VinculoLaboral vinculoLaboral = vinculoLaboralRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + command.getVinculoLaboralId()));

        var req = command.getVinculoLaboralRequest();
        vinculoLaboral.atualizar(
            req.getDescription(),
            Boolean.TRUE.equals(req.getCountsSeniority()),
            Boolean.TRUE.equals(req.getEligibleForProgression())
        );
        VinculoLaboral saved = vinculoLaboralRepository.save(vinculoLaboral);

        return ResponseEntity.ok(vinculoLaboralMapper.toDTO(saved));
    }
}

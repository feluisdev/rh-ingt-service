package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.OptionMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateOptionCommandHandler implements CommandHandler<UpdateOptionCommand, ResponseEntity<OptionResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateOptionCommandHandler.class);

    private final OptionRepository optionRepository;
    private final OptionMapper optionMapper;

    @IgrpCommandHandler
    public ResponseEntity<OptionResponseDTO> handle(UpdateOptionCommand command) {
        var dto = command.getOptionrequest();
        var id = ExternalID.from(java.util.UUID.fromString(command.getOptionId()));

        Option option = optionRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Etiqueta não encontrada: " + command.getOptionId()));

        option.atualizar(dto.getCvalue(), dto.getSortOrder(), dto.getDescription());

        Option updated = optionRepository.save(option);

        return ResponseEntity.ok(optionMapper.toDTO(updated));
    }
}

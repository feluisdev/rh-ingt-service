package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.OptionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DesativarOptionCommandHandler implements CommandHandler<DesativarOptionCommand, ResponseEntity<SuccessResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesativarOptionCommandHandler.class);

    private final OptionRepository optionRepository;

    @CacheEvict(value = "reference-options", allEntries = true)
    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(DesativarOptionCommand command) {
        var id = OptionId.from(command.getOptionId());

        Option option = optionRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Etiqueta não encontrada: " + command.getOptionId()));

        option.desativar();
        optionRepository.save(option);

        return ResponseEntity.ok(SuccessResponseDTO.de(command.getOptionId()));
    }
}

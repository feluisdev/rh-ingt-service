package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AtivarOptionCommandHandler implements CommandHandler<AtivarOptionCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtivarOptionCommandHandler.class);

    private final OptionRepository optionRepository;

    @CacheEvict(value = "reference-options", allEntries = true)
    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AtivarOptionCommand command) {
        var id = ExternalID.from(java.util.UUID.fromString(command.getOptionId()));

        Option option = optionRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Etiqueta não encontrada: " + command.getOptionId()));

        option.reativar();
        optionRepository.save(option);

        return ResponseEntity.ok(Map.of("message", "Etiqueta activada com sucesso"));
    }
}

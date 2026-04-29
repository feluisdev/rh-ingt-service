package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CreateOptionCommandHandler implements CommandHandler<CreateOptionCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateOptionCommandHandler.class);

    private final OptionRepository optionRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateOptionCommand command) {
        var dto = command.getOptionrequest();

        if (optionRepository.existsByCcodeAndCkeyAndLocale(dto.getCcode(), dto.getCkey(),
                dto.getLocale() != null ? dto.getLocale() : "pt-CV")) {
            throw IgrpResponseStatusException.conflict(
                "Já existe uma etiqueta com ccode='" + dto.getCcode() + "', ckey='" + dto.getCkey() + "'.");
        }

        Option option = Option.criar(
            dto.getCcode(),
            dto.getCkey(),
            dto.getCvalue(),
            dto.getLocale(),
            dto.getSortOrder(),
            dto.getDescription()
        );

        Option saved = optionRepository.save(option);

        return ResponseEntity.status(201).body(Map.of(
            "optionId", saved.getId().getStringValor(),
            "message", "Etiqueta criada com sucesso"
        ));
    }
}

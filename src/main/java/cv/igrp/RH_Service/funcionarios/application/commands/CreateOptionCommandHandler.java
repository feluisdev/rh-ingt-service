package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.models.Option;
import cv.igrp.RH_Service.funcionarios.domain.repository.OptionRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreateOptionCommandHandler implements CommandHandler<CreateOptionCommand, ResponseEntity<Map<String, ?>>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateOptionCommandHandler.class);

  private final OptionRepository optionRepository;

  public CreateOptionCommandHandler(OptionRepository optionRepository) {
    this.optionRepository = optionRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<Map<String, ?>> handle(CreateOptionCommand command) {
    var dto = command.getOptionrequest();

    if (optionRepository.existsByCkeyAndCcodeAndLocale(dto.getCkey(), dto.getCcode(), dto.getLocale())) {
      LOGGER.warn("Tentativa de criar Option com ccode '{}' e ckey '{}' que já existe para local '{}'.", dto.getCcode(), dto.getCkey(), dto.getLocale());

      throw IgrpResponseStatusException.badRequest("Option with ccode '" + dto.getCcode() + "' and ckey '" + dto.getCkey() + "' already exists for locale '" + dto.getLocale() + "'");
    }

    var option = Option.criar(
        dto.getCcode(),
        dto.getCkey(),
        dto.getCvalue(),
        dto.getLocale(),
        dto.getSort_order(),
        dto.isActive(),
        dto.getDescription()
    );

    var saved = optionRepository.save(option);

    Map<String, Object> response = Map.of(
        "optionId", saved.getIdOption().getStringValor(),
        "message", "Opção criada com sucesso"
    );

    LOGGER.info("Opção {} criada com sucesso", saved.getIdOption().getStringValor());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

}

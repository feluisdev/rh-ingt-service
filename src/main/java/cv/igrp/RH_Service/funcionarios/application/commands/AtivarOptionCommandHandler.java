package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.repository.OptionRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AtivarOptionCommandHandler implements CommandHandler<AtivarOptionCommand, ResponseEntity<Map<String, ?>>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AtivarOptionCommandHandler.class);

  private final OptionRepository optionRepository;

  public AtivarOptionCommandHandler(OptionRepository optionRepository) {
    this.optionRepository = optionRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<Map<String, ?>> handle(AtivarOptionCommand command) {
    var optionId = ExternalID.from(command.getOptionId());

    var option = optionRepository.getById(optionId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Option não encontrado com ID: " + optionId.getStringValor()));

    option.ativar();
    optionRepository.save(option);

    LOGGER.info("Opção {} ativada com sucesso", optionId.getStringValor());

    Map<String, Object> response = Map.of(
        "optionId", option.getIdOption().getStringValor(),
        "message", "Opção ativada com sucesso"
    );

    return ResponseEntity.ok(response);
  }

}
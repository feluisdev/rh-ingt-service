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

@Component("funcionariosDesativarOptionCommandHandler")
public class DesativarOptionCommandHandler implements CommandHandler<DesativarOptionCommand, ResponseEntity<Map<String, ?>>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DesativarOptionCommandHandler.class);

  private final OptionRepository optionRepository;

  public DesativarOptionCommandHandler(OptionRepository optionRepository) {
    this.optionRepository = optionRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<Map<String, ?>> handle(DesativarOptionCommand command) {
    var optionId = ExternalID.from(command.getOptionId());

    var option = optionRepository.getById(optionId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Option não encontrado com ID: " + optionId.getStringValor()));

    option.inativar();
    optionRepository.save(option);

    LOGGER.info("Opção {} desativada com sucesso", optionId.getStringValor());

    Map<String, Object> response = Map.of(
        "optionId", option.getIdOption().getStringValor(),
        "message", "Opção desativada com sucesso"
    );

    return ResponseEntity.ok(response);
  }

}

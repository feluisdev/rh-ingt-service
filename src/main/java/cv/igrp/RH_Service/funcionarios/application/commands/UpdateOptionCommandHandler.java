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

@Component
public class UpdateOptionCommandHandler implements CommandHandler<UpdateOptionCommand, ResponseEntity<String>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateOptionCommandHandler.class);

  private final OptionRepository optionRepository;

  public UpdateOptionCommandHandler(OptionRepository optionRepository) {
    this.optionRepository = optionRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<String> handle(UpdateOptionCommand command) {
    var optionId = ExternalID.from(command.getOptionId());
    var dto = command.getOptionrequest();

    var option = optionRepository.getById(optionId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Option não encontrado com ID: " + optionId.getStringValor()));

    option.atualizar(
        dto.getCcode(),
        dto.getCkey(),
        dto.getCvalue(),
        dto.getLocale(),
        dto.getSort_order(),
        dto.isActive(),
        dto.getDescription()
    );

    optionRepository.save(option);

    LOGGER.info("Opção {} atualizada com sucesso", optionId.getStringValor());

    return ResponseEntity.ok("Opção atualizada com sucesso");
  }

}

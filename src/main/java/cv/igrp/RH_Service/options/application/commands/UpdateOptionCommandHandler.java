package cv.igrp.RH_Service.options.application.commands;

import cv.igrp.RH_Service.options.domain.repository.OptionRepository;
import cv.igrp.RH_Service.options.domain.valueobject.Metadata;
import cv.igrp.RH_Service.options.domain.valueobject.OptionId;
import cv.igrp.RH_Service.options.infrastructure.mappers.OptionMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.options.application.dto.OptionResponseDTO;

@Component
public class UpdateOptionCommandHandler implements CommandHandler<UpdateOptionCommand, ResponseEntity<OptionResponseDTO>> {


  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateOptionCommandHandler.class);

  private final OptionRepository optionRepository;
  private final OptionMapper optionMapper;

  public UpdateOptionCommandHandler(OptionRepository optionRepository, OptionMapper optionMapper) {
    this.optionRepository = optionRepository;
    this.optionMapper = optionMapper;
  }

  @IgrpCommandHandler
  public ResponseEntity<OptionResponseDTO> handle(UpdateOptionCommand command) {

    var dto = command.getOptionrequest();
    var optionId = OptionId.from(command.getOptionId());


    var existingOption = optionRepository.findById(optionId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound(
            "Option not found for id: " + command.getOptionId()));


    var metadata = Metadata.fromMap(dto.getMetadata());
    // 3. Atualizar atributos principais
    existingOption.update(
        dto.getCcode(),
        dto.getCkey(),
        dto.getCvalue(),
        dto.getLocale(),
        dto.getSort_order(),
        metadata,
        ""
    );

    // 4. Persistir a atualização
    var saved = optionRepository.save(existingOption);

    // 5. Converter para DTO de resposta
    OptionResponseDTO responseDTO = optionMapper.toResponseDTO(saved);

    return ResponseEntity.ok(responseDTO);
  }

}

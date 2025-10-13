package cv.igrp.RH_Service.options.application.commands;

import cv.igrp.RH_Service.options.application.dto.OptionRequestDTO;
import cv.igrp.RH_Service.options.domain.models.Option;
import cv.igrp.RH_Service.options.domain.repository.OptionRepository;
import cv.igrp.RH_Service.options.domain.valueobject.Metadata;
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
public class CreateOptionsCommandHandler implements CommandHandler<CreateOptionsCommand, ResponseEntity<OptionResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(CreateOptionsCommandHandler.class);
  private final OptionRepository optionRepository;
  private final OptionMapper optionMapper;

  public CreateOptionsCommandHandler(OptionRepository optionRepository, OptionMapper optionMapper) {
    this.optionRepository = optionRepository;
    this.optionMapper = optionMapper;
  }

  @IgrpCommandHandler
  public ResponseEntity<OptionResponseDTO> handle(CreateOptionsCommand command) {
    OptionRequestDTO dto = command.getOptionrequest();

    // 1. Verificar se já existe Option com mesmo ccode + ckey (regra típica para opções)
    if (optionRepository.existsByCkeyAndCcodeAndLocale(dto.getCkey(), dto.getCcode(), dto.getLocale())) {
      throw IgrpResponseStatusException.badRequest(
          "Option with ccode '" + dto.getCcode() + "' and ckey '" + dto.getCkey() + "' and locale '" + dto.getLocale() + "' already exists");
    }

    Metadata metadata = Metadata.fromMap(dto.getMetadata());

    // 2. Criar o objeto de domínio
    Option option = Option.create(
        dto.getCcode(),
        dto.getCkey(),
        dto.getCvalue(),
        dto.getLocale(),
        dto.getSort_order(),
        metadata,
        dto.getDescription()
    );


    // 3. Persistir via repositório
    Option savedOption = optionRepository.save(option);

    // 4. Converter para DTO de resposta
    OptionResponseDTO responseDTO = optionMapper.toResponseDTO(savedOption);

    return ResponseEntity.ok(responseDTO);
  }
}

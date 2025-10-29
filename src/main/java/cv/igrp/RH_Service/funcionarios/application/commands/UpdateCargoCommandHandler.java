package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.repository.CargoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.CargoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.funcionarios.application.dto.CargoResponseDTO;

@Component
public class UpdateCargoCommandHandler implements CommandHandler<UpdateCargoCommand, ResponseEntity<CargoResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCargoCommandHandler.class);
  private final CargoRepository cargoRepository;
  private final CargoMapper cargoMapper;

  public UpdateCargoCommandHandler(CargoRepository cargoRepository, CargoMapper cargoMapper) {

    this.cargoRepository = cargoRepository;
    this.cargoMapper = cargoMapper;
  }

  @IgrpCommandHandler
  public ResponseEntity<CargoResponseDTO> handle(UpdateCargoCommand command) {
    var dto = command.getCargorequest();
    var cargoId = ExternalID.from(command.getCargoId());

    var cargo = cargoRepository.getById(cargoId).orElseThrow(
        () -> IgrpResponseStatusException.notFound("Cargo not found with ID: " + cargoId.getStringValor())
    );

    cargo.atualizar(
        dto.getNome(),
        dto.getCodigo(),
        dto.getDescricao(),
        dto.getSalarioBase(),
        dto.getNivelHierarquico()
    );

    var salvo = cargoRepository.save(cargo);

    var responseDto = cargoMapper.toDTO(salvo);

    return ResponseEntity.ok(responseDto);
  }

}

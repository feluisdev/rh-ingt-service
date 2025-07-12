package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.models.Cargo;
import cv.igrp.RH_Service.funcionarios.domain.repository.CargoRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component
public class AtivarCargoCommandHandler implements CommandHandler<AtivarCargoCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(AtivarCargoCommandHandler.class);

  private final CargoRepository cargoRepository;

  public AtivarCargoCommandHandler(CargoRepository cargoRepository) {

    this.cargoRepository = cargoRepository;
  }

   @IgrpCommandHandler
   public ResponseEntity<Map<String, ?>> handle(AtivarCargoCommand command) {
     var cargoId = ExternalID.from(command.getCargoId());

     Cargo cargo = cargoRepository.getByExternalId(cargoId)
         .orElseThrow(() -> IgrpResponseStatusException.notFound("Cargo não encontrado com ID: " + cargoId.getStringValor()));

     cargo.ativar();

     Cargo salvo = cargoRepository.save(cargo);

     Map<String, Object> response = Map.of(
         "cargoId", salvo.getExternalId().getStringValor(),
         "message", "Cargo ativado com sucesso"
     );

     return ResponseEntity.ok(response);
   }

}

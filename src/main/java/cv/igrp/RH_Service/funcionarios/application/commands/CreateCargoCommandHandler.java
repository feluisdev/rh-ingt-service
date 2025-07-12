package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.models.Cargo;
import cv.igrp.RH_Service.funcionarios.domain.repository.CargoRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component
public class CreateCargoCommandHandler implements CommandHandler<CreateCargoCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(CreateCargoCommandHandler.class);

   private final CargoRepository cargoRepository;
   public CreateCargoCommandHandler(CargoRepository cargoRepository) {

     this.cargoRepository = cargoRepository;
   }

   @IgrpCommandHandler
   public ResponseEntity<Map<String, ?>> handle(CreateCargoCommand command) {
     var dto = command.getCargorequest();

     Cargo novoCargo = Cargo.criarNovo(
         dto.getNome(),
         dto.getDescricao(),
         dto.getCodigo(),
         dto.getSalarioBase(),
         dto.getNivelHierarquico()
     );

     // Salvar usando repositório
     Cargo salvo = cargoRepository.save(novoCargo);

     // Retornar resposta com o ID externo criado
     Map<String, Object> response = Map.of(
         "cargoId", salvo.getExternalId().getStringValor(),
         "message", "Cargo criado com sucesso"
     );

     return ResponseEntity.ok(response);
   }

}

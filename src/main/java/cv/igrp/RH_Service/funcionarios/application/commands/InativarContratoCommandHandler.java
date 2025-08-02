package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Map;

@Component
public class InativarContratoCommandHandler implements CommandHandler<InativarContratoCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(InativarContratoCommandHandler.class);

  private final ContratoRepository contratoRepository;

   public InativarContratoCommandHandler(ContratoRepository contratoRepository) {

     this.contratoRepository = contratoRepository;
   }

   @IgrpCommandHandler
   public ResponseEntity<Map<String, ?>> handle(InativarContratoCommand command) {
     var externalId = ExternalID.from(command.getContratoId());


     var contrato = contratoRepository.getById(externalId)
         .orElseThrow(() -> IgrpResponseStatusException.of(
             HttpStatus.NOT_FOUND, "contrato não encontrado com id: " + externalId.getStringValor()
         ));


     contrato.desativar(LocalDate.now());

     contratoRepository.save(contrato);

     Map<String, Object> response = Map.of(
         "mensagem", "Contrato inativado com sucesso.",
         "contratoId", externalId.getStringValor()
     );

     return ResponseEntity.ok(response);

   }

}

package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.repository.DepartamentoRepository;
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
public class AtivarDepartamentoCommandHandler implements CommandHandler<AtivarDepartamentoCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(AtivarDepartamentoCommandHandler.class);

  private final DepartamentoRepository departamentoRepository;

  public AtivarDepartamentoCommandHandler(DepartamentoRepository departamentoRepository) {

    this.departamentoRepository = departamentoRepository;
  }

   @IgrpCommandHandler
   public ResponseEntity<Map<String, ?>> handle(AtivarDepartamentoCommand command) {
     var departamentoId = ExternalID.from(command.getDepartamentoId());

     var departamento = departamentoRepository.getById(departamentoId)
         .orElseThrow(() -> IgrpResponseStatusException.notFound("Departamento não encontrado com ID: " + departamentoId.getStringValor()));

     departamento.ativar();

     departamentoRepository.save(departamento);

     LOGGER.info("Departamento {} desativado com sucesso", departamentoId.getStringValor());

     Map<String, Object> response = Map.of(
         "departamentoId", departamento.getIdDepartamento().getStringValor(),
         "message", "Departamento desativado com sucesso"
     );

     return ResponseEntity.ok(response);
   }

}

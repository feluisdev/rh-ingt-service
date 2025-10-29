package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component
public class InativarFuncionarioCommandHandler implements CommandHandler<InativarFuncionarioCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(InativarFuncionarioCommandHandler.class);

  private final FuncionarioRepository funcionarioRepository;

   public InativarFuncionarioCommandHandler(FuncionarioRepository funcionarioRepository) {

     this.funcionarioRepository = funcionarioRepository;
   }

   @IgrpCommandHandler
   public ResponseEntity<Map<String, ?>> handle(InativarFuncionarioCommand command) {

     var funcionarioUuid = ExternalID.from(command.getFuncionarioId());

     var funcionario = funcionarioRepository.getById(funcionarioUuid).orElseThrow(
         () -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "funcionario not found")
     );

     funcionario.inativar();
     funcionarioRepository.save(funcionario);

     return ResponseEntity.ok(Map.of("message", "Funcionario inativado com sucesso"));

   }

}

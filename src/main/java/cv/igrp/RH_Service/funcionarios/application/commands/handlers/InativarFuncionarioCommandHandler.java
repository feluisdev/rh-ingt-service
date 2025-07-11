package cv.igrp.RH_Service.funcionarios.application.commands.handlers;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.RH_Service.funcionarios.application.commands.commands.InativarFuncionarioCommand;

import java.util.Map;

@Service
public class InativarFuncionarioCommandHandler implements CommandHandler<InativarFuncionarioCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(InativarFuncionarioCommandHandler.class);

   public InativarFuncionarioCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<Map<String, ?>> handle(InativarFuncionarioCommand command) {
      // TODO: Implement the command handling logic here
      return null;
   }

}
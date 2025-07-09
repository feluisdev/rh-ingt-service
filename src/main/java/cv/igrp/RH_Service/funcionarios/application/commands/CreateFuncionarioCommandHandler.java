package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.models.Funcionario;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.funcionarios.application.dto.FuncionarioResponseDTO;

@Component
public class CreateFuncionarioCommandHandler implements CommandHandler<CreateFuncionarioCommand, ResponseEntity<FuncionarioResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(CreateFuncionarioCommandHandler.class);

   private final FuncionarioMapper funcionarioMapper;
   private final FuncionarioRepository funcionarioRepository;

   public CreateFuncionarioCommandHandler(FuncionarioMapper funcionarioMapper, FuncionarioRepository funcionarioRepository) {

     this.funcionarioMapper = funcionarioMapper;
     this.funcionarioRepository = funcionarioRepository;
   }

   @IgrpCommandHandler
   public ResponseEntity<FuncionarioResponseDTO> handle(CreateFuncionarioCommand command) {
      // TODO: Implement the command handling logic here
     var dto = command.getFuncionariorequest();

     var funcionario = Funcionario.criar(dto.getNome(), dto.getNif(),
         dto.getNumSegurado(), dto.getNib(),
         dto.getEmail(), dto.getSexo(), dto.getEstadoCivil(), dto.getEndereco());


     var saved = funcionarioRepository.save(funcionario);

     return ResponseEntity.ok(funcionarioMapper.toResponseDTO(saved));
   }

}

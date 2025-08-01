package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DependenteMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.funcionarios.application.dto.DependenteResponseDTO;

@Component
public class UpdateDependenteCommandHandler implements CommandHandler<UpdateDependenteCommand, ResponseEntity<DependenteResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDependenteCommandHandler.class);

  private final DependenteRepository dependenteRepository;
  private final DependenteMapper dependenteMapper;

  private final FuncionarioRepository funcionarioRepository;

   public UpdateDependenteCommandHandler(DependenteRepository dependenteRepository, DependenteMapper dependenteMapper, FuncionarioRepository funcionarioRepository) {

     this.dependenteRepository = dependenteRepository;
     this.dependenteMapper = dependenteMapper;
     this.funcionarioRepository = funcionarioRepository;
   }

   @IgrpCommandHandler
   public ResponseEntity<DependenteResponseDTO> handle(UpdateDependenteCommand command) {

     var dependenteId = ExternalID.from(command.getDependenteId());
     var funcionarioId = ExternalID.from(command.getFuncionarioId());

     var funcionario = funcionarioRepository.getById(funcionarioId)
         .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Funcionário não encontrado: " + funcionarioId));

     var dependente = funcionario.getDependenteByExternalId(dependenteId);

     if(dependente == null) {
       throw IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Dependente não encontrado: " + dependenteId);
     }

     dependente.atualizarDados(
         command.getDependenterequest().getNome(),
         command.getDependenterequest().getDataNascimento(),
         command.getDependenterequest().getParentesco(),
         command.getDependenterequest().getCpf()
     );

     funcionarioRepository.save(funcionario);

     var responseDTO = dependenteMapper.toResponseDTO(dependente);

     return ResponseEntity.ok(responseDTO);

     /*var dependenteId = ExternalID.from(command.getDependenteId());
     var funcionarioId = ExternalID.from(command.getFuncionarioId());

     var dependente = dependenteRepository.getByExternalId(dependenteId)
         .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Dependente não encontrado: " + dependenteId));

     if (!dependente.getFuncionario().getExternalId().equals(funcionarioId)) {
       throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Dependente não pertence ao funcionário informado.");
     }

     dependente.atualizarDados(
         command.getDependenterequest().getNome(),
         command.getDependenterequest().getDataNascimento(),
         command.getDependenterequest().getParentesco(),
         command.getDependenterequest().getCpf()
     );

     dependenteRepository.save(dependente);

     var responseDTO = dependenteMapper.toResponseDTO(dependente);

     return ResponseEntity.ok(responseDTO);*/
   }

}

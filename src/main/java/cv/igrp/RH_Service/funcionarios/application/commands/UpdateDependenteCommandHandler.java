package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.repository.DependenteRepository;
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

   public UpdateDependenteCommandHandler(DependenteRepository dependenteRepository, DependenteMapper dependenteMapper) {

     this.dependenteRepository = dependenteRepository;
     this.dependenteMapper = dependenteMapper;
   }

   @IgrpCommandHandler
   public ResponseEntity<DependenteResponseDTO> handle(UpdateDependenteCommand command) {
     var dependenteId = ExternalID.from(command.getDependenteId());
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

     return ResponseEntity.ok(responseDTO);
   }

}

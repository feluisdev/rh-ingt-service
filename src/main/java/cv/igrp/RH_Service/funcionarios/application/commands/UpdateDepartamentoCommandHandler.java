package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.repository.DepartamentoRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DepartamentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.funcionarios.application.dto.DepartamentoResponseDTO;

@Component
public class UpdateDepartamentoCommandHandler implements CommandHandler<UpdateDepartamentoCommand, ResponseEntity<DepartamentoResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDepartamentoCommandHandler.class);

  private final DepartamentoRepository departamentoRepository;
  private final FuncionarioRepository funcionarioRepository;
  private final DepartamentoMapper departamentoMapper;

   public UpdateDepartamentoCommandHandler(DepartamentoRepository departamentoRepository, FuncionarioRepository funcionarioRepository, DepartamentoMapper departamentoMapper) {

     this.departamentoRepository = departamentoRepository;
     this.funcionarioRepository = funcionarioRepository;
     this.departamentoMapper = departamentoMapper;
   }

   @IgrpCommandHandler
   public ResponseEntity<DepartamentoResponseDTO> handle(UpdateDepartamentoCommand command) {
     var departamentoId = ExternalID.from(command.getDepartamentoId());
     var dto = command.getDepartamentorequest();

     var responsavelId = ExternalID.from(dto.getResponsavelId());

     var existeFuncionario = funcionarioRepository.existsById(responsavelId);

     if(!existeFuncionario)
       throw IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Funcionario not found with id: " + responsavelId);


     var departamento = departamentoRepository.getById(departamentoId).orElseThrow(
        () -> IgrpResponseStatusException.notFound("Departamento não encontrado com ID: " + departamentoId.getStringValor())
      );

      departamento.atualizar(
         dto.getNome(),
         dto.getCodigo(),
         dto.getDescricao(),
         dto.getLocalizacao(),
         dto.getOrcamento(),
          responsavelId
      );
      departamentoRepository.save(departamento);
     return ResponseEntity.ok(departamentoMapper.toDTO(departamento));
   }

}

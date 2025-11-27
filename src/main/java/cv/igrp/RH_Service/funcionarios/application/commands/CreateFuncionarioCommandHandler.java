package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoRequestDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Funcionario;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.TipoDocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.shared.application.constants.ObjetoTipo;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
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
  private final DocumentoMapper documentoMapper;

  private final TipoDocumentoRepository tipoDocumentoRepository;

   public CreateFuncionarioCommandHandler(FuncionarioMapper funcionarioMapper, FuncionarioRepository funcionarioRepository, DocumentoMapper documentoMapper, TipoDocumentoRepository tipoDocumentoRepository) {

     this.funcionarioMapper = funcionarioMapper;
     this.funcionarioRepository = funcionarioRepository;
     this.documentoMapper = documentoMapper;
     this.tipoDocumentoRepository = tipoDocumentoRepository;
   }

   @IgrpCommandHandler
   public ResponseEntity<FuncionarioResponseDTO> handle(CreateFuncionarioCommand command) {
     LOGGER.info("CreateFuncionarioCommandHandler :: command: {}", command);
     var dto = command.getFuncionariorequest();


     var funcionario = Funcionario.criar(dto.getNome(), dto.getNif(),
         dto.getNumSegurado(), dto.getNib(),
         dto.getEmail(), dto.getSexo(), dto.getEstadoCivil(), dto.getEndereco());

     if (dto.getAnexos() != null && !dto.getAnexos().isEmpty()) {
       for (DocumentoRequestDTO docDto : dto.getAnexos()) {

         var documento = documentoMapper.toDocumentoDomain(ObjetoTipo.FUNCIONARIO, funcionario.getIdFuncionario(), docDto);
         funcionario.adicionarDocumento(documento);
       }
     }

     var saved = funcionarioRepository.save(funcionario);

     return ResponseEntity.ok(funcionarioMapper.toResponseDTO(saved));
   }

}

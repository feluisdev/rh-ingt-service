package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoRequestDTO;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.TipoDocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.shared.application.constants.ObjetoTipo;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.funcionarios.application.dto.FuncionarioResponseDTO;

@Component
public class UpdateFuncionarioCommandHandler implements CommandHandler<UpdateFuncionarioCommand, ResponseEntity<FuncionarioResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFuncionarioCommandHandler.class);

  private final FuncionarioRepository funcionarioRepository;
  private final FuncionarioMapper funcionarioMapper;

  private final DocumentoMapper documentoMapper;

  private final TipoDocumentoRepository tipoDocumentoRepository;


  public UpdateFuncionarioCommandHandler(FuncionarioRepository funcionarioRepository, FuncionarioMapper funcionarioMapper, DocumentoMapper documentoMapper, TipoDocumentoRepository tipoDocumentoRepository) {

     this.funcionarioRepository = funcionarioRepository;
     this.funcionarioMapper = funcionarioMapper;
    this.documentoMapper = documentoMapper;
    this.tipoDocumentoRepository = tipoDocumentoRepository;
  }

   @IgrpCommandHandler
   public ResponseEntity<FuncionarioResponseDTO> handle(UpdateFuncionarioCommand command) {
     var funcionarioUuid = ExternalID.from(command.getFuncionarioId());

     var funcionario = funcionarioRepository.getById(funcionarioUuid).orElseThrow(
         () -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "funcionario not found")
     );
     var dtoRequest = command.getFuncionariorequest();

   funcionario.atualizar(dtoRequest.getNome(), dtoRequest.getNif(),
         dtoRequest.getNumSegurado(), dtoRequest.getNib(), dtoRequest.getEmail(),
         dtoRequest.getSexo(), dtoRequest.getEstadoCivil(), dtoRequest.getEndereco()
         );

     if (dtoRequest.getAnexos() != null && !dtoRequest.getAnexos().isEmpty()) {
       for (DocumentoRequestDTO docDto : dtoRequest.getAnexos()) {
         var tipoDocumento = tipoDocumentoRepository.getById(ExternalID.from(docDto.getIdTipodocumento()))
             .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo documento not found with id:: "+docDto.getIdTipodocumento()));

         var documento = documentoMapper.toDocumentoDomain(ObjetoTipo.FUNCIONARIO, funcionario.getIdFuncionario(), docDto, tipoDocumento);
         funcionario.adicionarOuAtualizarDocumento(documento);
       }
     }

    var updatedFuncionario = funcionarioRepository.save(funcionario);

      return ResponseEntity.ok(funcionarioMapper.toResponseDTO(updatedFuncionario));
   }

}

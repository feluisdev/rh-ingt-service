package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Component
public class UpdateDocumentoCommandHandler implements CommandHandler<UpdateDocumentoCommand, ResponseEntity<DocumentoResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDocumentoCommandHandler.class);
  private final DocumentoRepository documentoRepository;
  private final DocumentoMapper documentoMapper;
   public UpdateDocumentoCommandHandler(DocumentoRepository documentoRepository, DocumentoMapper documentoMapper) {
     this.documentoRepository = documentoRepository;
     this.documentoMapper = documentoMapper;
   }

   @IgrpCommandHandler
   public ResponseEntity<DocumentoResponseDTO> handle(UpdateDocumentoCommand command) {
      // TODO: Implement the command handling logic here
     var dto = command.getDocumentorequest();
     var tipoDocumentoId = ExternalID.from(command.getDocumentoId());

     var tipoDocumento = documentoRepository.getByExternalId(tipoDocumentoId).orElseThrow(

         () -> IgrpResponseStatusException.notFound("Documento not found with ID: " + tipoDocumentoId.getStringValor())
     );

     tipoDocumento.atualizar(
         dto.getUrl(),
         dto.getObservacao(),
         dto.getEstado()
     );

     var salvo = documentoRepository.save(tipoDocumento);

     var responseDto = documentoMapper.toDTO(salvo);

     return ResponseEntity.ok(responseDto);

   }

}

package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoRequestDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.TipoDocumento;
import cv.igrp.RH_Service.funcionarios.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.TipoDocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.TipoDocumentoMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Component
public class CreateTipoDocumentoCommandHandler implements CommandHandler<CreateTipoDocumentoCommand, ResponseEntity<TipoDocumentoResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(CreateTipoDocumentoCommandHandler.class);
  private final TipoDocumentoRepository documentoRepository;
  private final TipoDocumentoMapper documentoMapper;

   public CreateTipoDocumentoCommandHandler(TipoDocumentoRepository documentoRepository,TipoDocumentoMapper documentoMapper) {
     this.documentoRepository = documentoRepository;
     this.documentoMapper = documentoMapper;
   }

   @IgrpCommandHandler
   public ResponseEntity<TipoDocumentoResponseDTO> handle(CreateTipoDocumentoCommand command) {
      // TODO: Implement the command handling logic here
     var dto = command.getTipodocumentorequest();
     TipoDocumento tipoDocumento = TipoDocumento.criar(dto.getDescrcicao(),dto.getCodigo()
     );

     var saved = documentoRepository.save(tipoDocumento);

     var responseDTO = documentoMapper.toDto(saved);

     return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
   }

}

package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoRequestDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Documento;
import cv.igrp.RH_Service.funcionarios.domain.repository.CargoRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class CreateDocumentoCommandHandler implements CommandHandler<CreateDocumentoCommand, ResponseEntity<DocumentoResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateDocumentoCommandHandler.class);
  private final DocumentoRepository documentoRepository;
  private final DocumentoMapper documentoMapper;


  public CreateDocumentoCommandHandler(DocumentoRepository documentoRepository,DocumentoMapper documentoMapper) {
    this.documentoRepository = documentoRepository;
    this.documentoMapper = documentoMapper;
  }

  @IgrpCommandHandler
  public ResponseEntity<DocumentoResponseDTO> handle(CreateDocumentoCommand command) {
    // TODO: Implement the command handling logic here
   /* DocumentoRequestDTO dto = command.getDocumentorequest();

    var documento = Documento.criar(
        dto.getUrl(),
        dto.getObservacao(),
        dto.getObjecto_tipo(),
        Integer.valueOf(dto.getObjecto_id()), dto.getEstado()
    );

    var saved = documentoRepository.save(documento);

    DocumentoResponseDTO responseDTO = documentoMapper.toDTO(saved);

    return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);*/

    return  null;


  }

}

package cv.igrp.RH_Service.shared.interfaces.rest;


import cv.igrp.RH_Service.shared.application.constants.DocumentoFolder;
import cv.igrp.RH_Service.shared.application.dto.FileResponseDTO;
import cv.igrp.RH_Service.shared.application.dto.FileUrlDTO;
import cv.igrp.RH_Service.shared.domain.service.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("documento")
@Tag(name = "Documento", description = "API para gestão e manipulação de documentos no sistema")
public class DocumentController {

  private final DocumentoService documentoService;

  public DocumentController(DocumentoService documentoService) {
    this.documentoService = documentoService;
  }

  @PostMapping(
      path = "private/{folder}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @Operation(
      summary = "Upload de documento privado",
      description = "Realiza o upload de um documento para uma pasta privada específica do sistema",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Documento enviado com sucesso",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FileResponseDTO.class))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Ficheiro inválido ou vazio",
              content = @Content
          )
      }
  )
  public ResponseEntity<FileResponseDTO> upload(
      @PathVariable DocumentoFolder folder,
      @Parameter(description = "File to upload", required = true) @RequestParam(name = "file") MultipartFile uploadFile) {
    return documentoService.save(folder, uploadFile);
  }

  @PostMapping(
      path = "public/{path}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @Operation(
      summary = "Upload de documento público",
      description = "Realiza o upload de um documento para uma pasta pública específica com URL acessível",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Documento enviado com sucesso",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FileResponseDTO.class))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Ficheiro inválido ou vazio",
              content = @Content
          )
      }
  )
  public ResponseEntity<FileResponseDTO> uploadPublicFile(
      @PathVariable String path, @Parameter(description = "File to upload", required = true) @RequestParam(name = "file") MultipartFile file) {
    return documentoService.savePublicFile(path, file);
  }

  @GetMapping
  @Operation(
      summary = "Obter link",
      description = "Gera um link assinado temporário para acesso ao documento",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Link gerado com sucesso",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FileUrlDTO.class))
          )
      }
  )
  public ResponseEntity<FileUrlDTO> getPresignedLink(@Parameter(description = "ID do ficheiro para gerar o link", required = true) @RequestParam(value = "fileId") String fileId) {
    return documentoService.getPresignedLink(fileId);
  }

}

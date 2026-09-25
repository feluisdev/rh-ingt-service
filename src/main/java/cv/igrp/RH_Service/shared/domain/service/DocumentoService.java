package cv.igrp.RH_Service.shared.domain.service;

import cv.igrp.RH_Service.shared.application.constants.DocumentoFolder;
import cv.igrp.RH_Service.shared.application.dto.FileResponseDTO;
import cv.igrp.RH_Service.shared.application.dto.FileUrlDTO;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.filemanager.minio.MinioService;
import cv.igrp.framework.filemanager.minio.MinioStorage;
import org.springframework.http.HttpStatus;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentoService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentoService.class);
  private static final String PATH_SEPARATOR = "/";
  private final MinioStorage minioService;

  public DocumentoService(MinioService minioService) {
    this.minioService = minioService;
  }

  public ResponseEntity<FileResponseDTO> save(DocumentoFolder folder, MultipartFile file) {
    if (file == null || file.isEmpty())
      throw IgrpResponseStatusException.badRequest("Invalid file submitted");

    try {
      var uniqueFilename = buildUniqueFilename(file.getOriginalFilename());
      var uniqueFilePath = "%s/%s".formatted(DocumentoFolder.ouOutros(folder).getCode(), uniqueFilename);

      minioService.uploadFile(
          file.getBytes(),
          uniqueFilePath,
          file.getContentType()
      );

      var fileResponse = new FileResponseDTO(
          file.getOriginalFilename(),
          uniqueFilePath
      );

      return ResponseEntity.ok().body(fileResponse);
    } catch (Exception e) {
      LOGGER.error("Falha ao guardar o ficheiro '{}' na pasta {}", file.getOriginalFilename(), folder, e);
      throw IgrpResponseStatusException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao guardar o ficheiro");
    }
  }

  public ResponseEntity<FileResponseDTO> savePublicFile(String path, MultipartFile file) {
    if (file == null || file.isEmpty())
      throw IgrpResponseStatusException.badRequest("Invalid file submitted");

    try {
      var pathProcessed = path.replace(".", "/");

      var uniqueFilename = pathProcessed + PATH_SEPARATOR + buildUniqueFilename(file.getOriginalFilename());

      minioService.uploadPublicFile(
          file.getBytes(),
          uniqueFilename,
          file.getContentType()
      );

      var url = minioService.getMinioProperties().url();

      var bucketName = minioService.getMinioProperties().bucketName();

      var fileId = "%s/%s/%s".formatted(url, bucketName, uniqueFilename);

      var fileResponse = new FileResponseDTO(
          file.getOriginalFilename(),
          fileId
      );

      return ResponseEntity.ok().body(fileResponse);
    } catch (Exception e) {
      LOGGER.error("Falha ao guardar o ficheiro público '{}' em {}", file.getOriginalFilename(), path, e);
      throw IgrpResponseStatusException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao guardar o ficheiro público");
    }
  }

  private String buildUniqueFilename(String name) {

    var baseName = FilenameUtils.getBaseName(name);

    var extension = FilenameUtils.getExtension(name);

    return "%s_%s.%s".formatted(
        baseName,
        System.currentTimeMillis(),
        extension
    );
  }

  /**
   * Grava no MinIO um ficheiro gerado pelo servidor (ex.: o PDF de uma declaração) e devolve o caminho.
   * {@code nome} é o nome legível (ex.: {@code DEC-2026-000001.pdf}); o caminho fica único.
   */
  public String guardarGerado(DocumentoFolder folder, String nome, byte[] conteudo, String contentType) {
    try {
      var caminho = "%s/%s".formatted(DocumentoFolder.ouOutros(folder).getCode(), buildUniqueFilename(nome));
      minioService.uploadFile(conteudo, caminho, contentType);
      return caminho;
    } catch (Exception e) {
      LOGGER.error("Falha ao guardar o ficheiro gerado '{}' na pasta {}", nome, folder, e);
      throw IgrpResponseStatusException.of(HttpStatus.INTERNAL_SERVER_ERROR,
          "Não foi possível guardar o documento. Tente de novo dentro de momentos.");
    }
  }

  public ResponseEntity<FileUrlDTO> getPresignedLink(String fileId) {
    try {
      var url = minioService.getFileUrl(fileId);

      var fileUrl = new FileUrlDTO(url);

      return ResponseEntity.ok().body(fileUrl);
    } catch (Exception e) {
      LOGGER.error("Falha ao obter URL assinado para {}", fileId, e);
      throw IgrpResponseStatusException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao obter URL assinado");
    }
  }
}

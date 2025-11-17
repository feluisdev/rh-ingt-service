package cv.igrp.RH_Service.shared.domain.service;

import cv.igrp.RH_Service.shared.application.constants.DocumentoFolder;
import cv.igrp.RH_Service.shared.application.dto.FileResponseDTO;
import cv.igrp.RH_Service.shared.application.dto.FileUrlDTO;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.filemanager.minio.MinioService;
import cv.igrp.framework.filemanager.minio.MinioStorage;
import org.springframework.http.HttpStatus;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentoService {

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
      var uniqueFilePath = "%s/%s".formatted(folder.getCode(), uniqueFilename);

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

  public ResponseEntity<FileUrlDTO> getPresignedLink(String fileId) {
    try {
      var url = minioService.getFileUrl(fileId);

      var fileUrl = new FileUrlDTO(url);

      return ResponseEntity.ok().body(fileUrl);
    } catch (Exception e) {
      throw IgrpResponseStatusException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao obter URL assinado");
    }
  }
}

package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.service.ExportDocumentHelper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapEvaluationEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.specifications.SiadapEvaluationSpecifications;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;

@Component
public class GetSiadapExportQueryHandler
    implements QueryHandler<GetSiadapExportQuery, ResponseEntity<byte[]>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetSiadapExportQueryHandler.class);

  private final SiadapEvaluationEntityRepository repository;
  private final FuncionarioLookupPort funcionarioLookupPort;
  private final OrganicaLookupPort organicaLookupPort;

  public GetSiadapExportQueryHandler(SiadapEvaluationEntityRepository repository,
                                    FuncionarioLookupPort funcionarioLookupPort,
                                    OrganicaLookupPort organicaLookupPort) {
    this.repository = repository;
    this.funcionarioLookupPort = funcionarioLookupPort;
    this.organicaLookupPort = organicaLookupPort;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<byte[]> handle(GetSiadapExportQuery query) {
    LOGGER.debug("GetSiadapExportQuery: {}", query);

    String format = query.getFormat() != null ? query.getFormat().toUpperCase() : "EXCEL";
    String filename = "SIADAP_" + query.getYear();

    Specification<SiadapEvaluationEntity> spec =
        SiadapEvaluationSpecifications.byFilters(query.getYear(), query.getOrganicUnitId(), null, null);
    List<SiadapEvaluationEntity> evaluations = repository.findAll(spec);

    if ("PDF_BATCH".equals(format)) {
      byte[] zipContent = generatePdfBatchZip(query.getYear(), evaluations);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType("application/zip"));
      headers.setContentDispositionFormData("attachment", filename + ".zip");
      return ResponseEntity.ok().headers(headers).body(zipContent);
    }

    // Default: EXCEL (CSV com BOM UTF-8)
    List<String[]> rows = new ArrayList<>();
    rows.add(new String[]{"MAPA DE AVALIACOES SIADAP - ANO " + query.getYear()});
    rows.add(new String[]{"Total de Avaliacoes: " + evaluations.size()});
    rows.add(new String[]{""});
    rows.add(new String[]{"ID Avaliacao", "Colaborador", "Avaliador", "Unidade Organica", "Fase", "Nota Objetivos", "Nota Competencias", "Nota Final", "Mencao de Merito"});

    for (SiadapEvaluationEntity entity : evaluations) {
      UUID empUuid = parseUuid(entity.getEmployeeId());
      String empName = empUuid != null
          ? funcionarioLookupPort.findById(empUuid).map(FuncionarioDTO::getNomeCompleto).orElse(entity.getEmployeeId())
          : (entity.getEmployeeId() != null ? entity.getEmployeeId() : "");

      UUID evalUuid = parseUuid(entity.getEvaluatorId());
      String evalName = evalUuid != null
          ? funcionarioLookupPort.findById(evalUuid).map(FuncionarioDTO::getNomeCompleto).orElse(entity.getEvaluatorId())
          : (entity.getEvaluatorId() != null ? entity.getEvaluatorId() : "");

      UUID unitUuid = parseUuid(entity.getOrganicUnitId());
      String unitName = unitUuid != null
          ? organicaLookupPort.findById(unitUuid).map(OrganicaDTO::getName).orElse(entity.getOrganicUnitId())
          : "Sem Unidade";

      rows.add(new String[]{
          entity.getId() != null ? entity.getId().toString() : "",
          empName,
          evalName,
          unitName,
          entity.getEvaluationPhase() != null ? entity.getEvaluationPhase() : "",
          entity.getObjectivesScore() != null ? entity.getObjectivesScore().toString() : "-",
          entity.getCompetenciesScore() != null ? entity.getCompetenciesScore().toString() : "-",
          entity.getFinalScore() != null ? entity.getFinalScore().toString() : "-",
          entity.getMeritRating() != null ? entity.getMeritRating() : "-"
      });
    }

    byte[] content = ExportDocumentHelper.createCsv(rows);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    headers.setContentDispositionFormData("attachment", filename + ".xlsx");
    return ResponseEntity.ok().headers(headers).body(content);
  }

  private byte[] generatePdfBatchZip(int year, List<SiadapEvaluationEntity> evaluations) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ZipOutputStream zos = new ZipOutputStream(baos)) {

      for (SiadapEvaluationEntity entity : evaluations) {
        UUID empUuid = parseUuid(entity.getEmployeeId());
        String empName = empUuid != null
            ? funcionarioLookupPort.findById(empUuid).map(FuncionarioDTO::getNomeCompleto).orElse(entity.getEmployeeId())
            : (entity.getEmployeeId() != null ? entity.getEmployeeId() : "colaborador");
        String safeName = empName.replaceAll("[^a-zA-Z0-9_-]", "_");

        List<String> lines = new ArrayList<>();
        lines.add("Ficha Individual SIADAP - Ano " + year);
        lines.add("Colaborador: " + empName);
        lines.add("Fase Atual: " + (entity.getEvaluationPhase() != null ? entity.getEvaluationPhase() : "-"));
        lines.add("Nota Objetivos: " + (entity.getObjectivesScore() != null ? entity.getObjectivesScore().toString() : "-"));
        lines.add("Nota Competencias: " + (entity.getCompetenciesScore() != null ? entity.getCompetenciesScore().toString() : "-"));
        lines.add("Nota Final: " + (entity.getFinalScore() != null ? entity.getFinalScore().toString() : "-"));
        lines.add("Mencao de Merito: " + (entity.getMeritRating() != null ? entity.getMeritRating() : "-"));

        byte[] pdfBytes = ExportDocumentHelper.createSimplePdf("FICHA SIADAP: " + empName, lines);
        ZipEntry entry = new ZipEntry("SIADAP_" + year + "_" + safeName + "_" + entity.getId() + ".pdf");
        zos.putNextEntry(entry);
        zos.write(pdfBytes);
        zos.closeEntry();
      }

      zos.finish();
      return baos.toByteArray();
    } catch (IOException e) {
      LOGGER.error("Error creating SIADAP PDF batch zip: {}", e.getMessage(), e);
      return new byte[0];
    }
  }

  private UUID parseUuid(String str) {
    if (str == null || str.isBlank()) return null;
    try {
      return UUID.fromString(str.trim());
    } catch (Exception e) {
      return null;
    }
  }
}

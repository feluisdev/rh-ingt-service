package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.service.ExportDocumentHelper;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationResult;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationScenario;
import cv.igrp.RH_Service.sigdi.domain.intelligence.repository.SimulationResultRepository;
import cv.igrp.RH_Service.sigdi.domain.intelligence.repository.SimulationScenarioRepository;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class GetScenarioExportQueryHandler
    implements QueryHandler<GetScenarioExportQuery, ResponseEntity<byte[]>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetScenarioExportQueryHandler.class);

  private final SimulationScenarioRepository scenarioRepository;
  private final SimulationResultRepository resultRepository;

  public GetScenarioExportQueryHandler(SimulationScenarioRepository scenarioRepository,
                                       SimulationResultRepository resultRepository) {
    this.scenarioRepository = scenarioRepository;
    this.resultRepository = resultRepository;
  }

  @IgrpQueryHandler
  public ResponseEntity<byte[]> handle(GetScenarioExportQuery query) {
    LOGGER.debug("GetScenarioExportQuery: {}", query);

    SimulationScenarioId id = SimulationScenarioId.from(UUID.fromString(query.getScenarioId()));

    SimulationScenario scenario = scenarioRepository.findById(id)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Scenario not found: " + query.getScenarioId()));

    List<SimulationResult> results = resultRepository.findByScenarioId(id);

    String format = query.getFormat() != null ? query.getFormat().toUpperCase() : "PDF";
    String filename = "scenario-" + query.getScenarioId();

    if ("EXCEL".equals(format) || "CSV".equals(format)) {
      List<String[]> rows = new ArrayList<>();
      rows.add(new String[]{"RELATORIO DE SIMULACAO DE CENARIO"});
      rows.add(new String[]{"Nome do Cenario", scenario.getName()});
      rows.add(new String[]{"Tipo", scenario.getParameters() != null ? scenario.getParameters().getType().getCode() : "-"});
      rows.add(new String[]{"Percentagem Ajuste", scenario.getParameters() != null ? scenario.getParameters().getPercentage() + "%" : "-"});
      rows.add(new String[]{"Total de Atividades", String.valueOf(results.size())});
      rows.add(new String[]{""});
      rows.add(new String[]{"ID Atividade", "Score de Impacto", "Recomendacao", "Detalhes"});

      for (SimulationResult r : results) {
        rows.add(new String[]{
            r.getActivityId() != null ? r.getActivityId().toString() : "",
            r.getImpactScore() != null ? r.getImpactScore().toString() : "0",
            r.getRecommendation() != null ? r.getRecommendation().name() : "-",
            r.getDetails() != null ? r.getDetails() : ""
        });
      }

      byte[] content = ExportDocumentHelper.createCsv(rows);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
      headers.setContentDispositionFormData("attachment", filename + ".xlsx");
      return ResponseEntity.ok().headers(headers).body(content);
    }

    // PDF format
    List<String> lines = new ArrayList<>();
    lines.add("Cenario: " + scenario.getName());
    lines.add("Tipo: " + (scenario.getParameters() != null ? scenario.getParameters().getType().getCode() : "-"));
    lines.add("Ajuste: " + (scenario.getParameters() != null ? scenario.getParameters().getPercentage() + "%" : "-"));
    lines.add("Total de Atividades Impactadas: " + results.size());
    lines.add("----------------------------------------------------------------------");
    lines.add(String.format("%-36s | %-10s | %-15s", "ID Atividade", "Score", "Recomendacao"));
    lines.add("----------------------------------------------------------------------");

    for (SimulationResult r : results) {
      lines.add(String.format("%-36s | %-10s | %-15s",
          truncate(r.getActivityId() != null ? r.getActivityId().toString() : "", 36),
          r.getImpactScore() != null ? r.getImpactScore().toString() : "0",
          r.getRecommendation() != null ? r.getRecommendation().name() : "-"));
    }

    byte[] content = ExportDocumentHelper.createSimplePdf("RELATORIO DE IMPACTO: " + scenario.getName(), lines);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", filename + ".pdf");
    return ResponseEntity.ok().headers(headers).body(content);
  }

  private String truncate(String text, int max) {
    if (text == null) return "";
    return text.length() > max ? text.substring(0, max - 3) + "..." : text;
  }
}

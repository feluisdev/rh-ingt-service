package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
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

import java.util.UUID;

@Component
public class GetScenarioExportQueryHandler
    implements QueryHandler<GetScenarioExportQuery, ResponseEntity<byte[]>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetScenarioExportQueryHandler.class);

  private final SimulationScenarioRepository scenarioRepository;

  public GetScenarioExportQueryHandler(SimulationScenarioRepository scenarioRepository) {
    this.scenarioRepository = scenarioRepository;
  }

  @IgrpQueryHandler
  public ResponseEntity<byte[]> handle(GetScenarioExportQuery query) {
    LOGGER.debug("GetScenarioExportQuery: {}", query);

    SimulationScenarioId id = SimulationScenarioId.from(UUID.fromString(query.getScenarioId()));

    scenarioRepository.findById(id)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Scenario not found: " + query.getScenarioId()));

    String format = query.getFormat() != null ? query.getFormat().toUpperCase() : "PDF";

    if ("EXCEL".equals(format)) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
      headers.setContentDispositionFormData("attachment", "scenario-" + query.getScenarioId() + ".xlsx");
      return ResponseEntity.ok().headers(headers).body(new byte[0]);
    }

    // Default: PDF stub
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", "scenario-" + query.getScenarioId() + ".pdf");
    return ResponseEntity.ok().headers(headers).body(new byte[0]);
  }
}

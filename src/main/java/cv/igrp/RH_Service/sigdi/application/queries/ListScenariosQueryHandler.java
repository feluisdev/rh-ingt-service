package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.ScenarioListItemDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperScenarioListDTO;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationScenario;
import cv.igrp.RH_Service.sigdi.domain.intelligence.repository.SimulationScenarioRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListScenariosQueryHandler
    implements QueryHandler<ListScenariosQuery, ResponseEntity<WrapperScenarioListDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListScenariosQueryHandler.class);

  private final SimulationScenarioRepository scenarioRepository;

  public ListScenariosQueryHandler(SimulationScenarioRepository scenarioRepository) {
    this.scenarioRepository = scenarioRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<WrapperScenarioListDTO> handle(ListScenariosQuery query) {
    LOGGER.debug("ListScenariosQuery: {}", query);

    int page = parseIntOrDefault(query.getPageNumber(), 0);
    int size = parseIntOrDefault(query.getPageSize(), 20);

    List<SimulationScenario> scenarios = scenarioRepository.findAll(page, size);
    long total = scenarioRepository.count();
    int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;

    List<ScenarioListItemDTO> items = scenarios.stream()
        .map(this::toListItem)
        .collect(Collectors.toList());

    WrapperScenarioListDTO response = new WrapperScenarioListDTO();
    response.setData(items);
    response.setPage(page);
    response.setSize(size);
    response.setTotalElements(total);
    response.setTotalPages(totalPages);

    return ResponseEntity.ok(response);
  }

  private ScenarioListItemDTO toListItem(SimulationScenario scenario) {
    ScenarioListItemDTO item = new ScenarioListItemDTO();
    item.setId(scenario.getId().getStringValor());
    item.setName(scenario.getName());
    item.setType(scenario.getParameters().getType().getCode());
    item.setPercentage(scenario.getParameters().getPercentage());
    item.setStatus(scenario.getStatus().getCode());
    return item;
  }

  private int parseIntOrDefault(String value, int defaultValue) {
    if (value == null || value.isBlank()) return defaultValue;
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}

package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.InstitutionResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.admin.models.Institution;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.InstitutionRepository;
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
public class GetListInstitutionsQueryHandler
    implements QueryHandler<GetListInstitutionsQuery, ResponseEntity<List<InstitutionResponseDTO>>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetListInstitutionsQueryHandler.class);

  private final InstitutionRepository institutionRepository;

  public GetListInstitutionsQueryHandler(InstitutionRepository institutionRepository) {
    this.institutionRepository = institutionRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<List<InstitutionResponseDTO>> handle(GetListInstitutionsQuery query) {
    LOGGER.debug("GetListInstitutionsQuery: {}", query);

    List<Institution> institutions = institutionRepository.findAll();

    List<InstitutionResponseDTO> result = institutions.stream()
        .filter(inst -> {
          if (query.getName() != null && !query.getName().isBlank()) {
            if (!inst.getName().toLowerCase().contains(query.getName().toLowerCase())) return false;
          }
          if (query.getType() != null && !query.getType().isBlank()) {
            if (!inst.getType().equalsIgnoreCase(query.getType())) return false;
          }
          if (query.getIsActive() != null) {
            if (inst.isActive() != query.getIsActive()) return false;
          }
          return true;
        })
        .map(inst -> {
          InstitutionResponseDTO dto = new InstitutionResponseDTO();
          dto.setId(inst.getId().getStringValor());
          dto.setCode(inst.getCode());
          dto.setName(inst.getName());
          dto.setType(inst.getType());
          dto.setIsActive(inst.isActive());
          dto.setContactEmail(inst.getContactEmail());
          dto.setCreatedAt(null);
          return dto;
        })
        .collect(Collectors.toList());

    return ResponseEntity.ok(result);
  }
}
